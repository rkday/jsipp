package uk.me.rkd.jsipp.runtime;

import java.nio.channels.DatagramChannel;

public class RTPSession {

	private String id;
	private RTPPacket lastPacket;
	private double lastTS;
	private double jitter = 0;
	private long lastPrint = 0;
	private long initialSeqNumber = 0;
	private long packetsSeen = 0;
	private long outOfSequencePackets = 0;
    private DatagramChannel channel;

	public RTPSession(String arbitraryId, DatagramChannel c) {
		this.id = arbitraryId;
		this.channel = c;
	}
	
	public String getId() {
        return id;
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public void update(RTPPacket packet) {
		double timestamp = System.currentTimeMillis() / 0.125;
		if (this.lastPacket != null) {
		    // Receiving a second or subsequent packet
            this.packetsSeen++;

			if (lastPacket.getSequenceNumber() > packet.getSequenceNumber()) {
			    // Packet received out of order - count this but do no other processing
				this.outOfSequencePackets++;
			} else {
			    // Jitter calculations
	            double deviation = (packet.getTimestamp() - timestamp) - (this.lastPacket.getTimestamp() - this.lastTS);
	            this.jitter = this.jitter + ((Math.abs(deviation) - this.jitter) / 16);
			    
	            // Remember this packet's data
			    this.lastPacket = packet;
			    this.lastTS = timestamp;
			}
		} else {
		    // First packet - set things up
            this.lastTS = timestamp;
            this.lastPacket = packet;		    
			this.initialSeqNumber = packet.getSequenceNumber() - 1;
            this.packetsSeen = 1;
        }
				
        if ((System.currentTimeMillis() - this.lastPrint) > 250) {
            String report_timestamp = Double.toString(System.currentTimeMillis() % 1000.0);
            
            // Report current jitter, packets received and packets lost/received out of order as a fraction of packets received
            double packetsLost = 1 - (packetsSeen / (packet.getSequenceNumber() - this.initialSeqNumber));
            Statistics.INSTANCE.report(Statistics.StatType.RTCP, report_timestamp, this.id, Double.toString(jitter), Long.toString(packetsSeen), Double.toString(packetsLost), Double.toString(outOfSequencePackets / packetsSeen), "0");
            this.lastPrint = System.currentTimeMillis();
        }
	}
	
}
