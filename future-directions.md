## Networking protocols

UDP, TCP and SCTP should all be pretty easy to support - these are just different channel types in the Java Selectors API.

It would be useful in future to support SIP over WebSockets, so SIPp could be used for testing WebRTC platforms.

## Call correlation

One of SIPp's weaknesses has been that it only correlates messages to a scenario based on the Call-ID. This made it very difficult to do things like register and then receive an INVITE - because the REGISTER message would have one Call-ID, but the incoming INVITE would have an entirely different Call-ID and SIPp would be unable to correlate it.

It should be possible to solve this by having a secondary correlation on Request-URI - so that if an incoming INVITE doesn't match any known Call-IDs, the Request-URI is checked, and it's matched to the scenario using that Request-URI. This would require scenarios to indicate what Request-URI they're using, and use it correctly in their Contact headers, but that shouldn't be too arduous a requirement.

It would also be useful to allow a second registration scenario to be specified, with its own rate, and having jSIPp keep the two in sync (e.g. ensuring that registrations for a particular user defined in a CSV file always use the same TCP connection as calls for those registrations). This is only a partial solution to a more general problem (see the 'IMS Bench' section below for more) but will be easier to implement and will significantly simplify a common use-case.

## RTP

The C++-based SIPp had two options for media handling: playing back captured RTP packets from a PCAP file, and streaming audio files over RTP (the RTP stream feature included in SIPp 3.4). Streaming audio files gave higher performance and was more flexible (and didn't have dependencies on external PCAP libraries or require specific types of capture files), so that seems like the best approach to start with.

A good way to do it might be to integrate the efflux library (https://github.com/brunodecarvalho/efflux), a pure-Java RTP stack.

PCAP-based media could probably be done with PCAP4j (https://github.com/kaitoy/pcap4j).

## IMS Bench

The key additional features of IMSBench are:

* The ability to have a "master" node oversee several SIPp nodes and correlate their test results.

This could be done quite easily - the master node could simply subscribe to the ZeroMQ statistics streams of the other nodes, and correlate them into a report. Inter-node communication should be possible using a HTTP or ZeroMQ request/response interface.

* A concept of multiple users, who are grouped into "pools" based on their state (registered, unregistered, in a call, etc.)

Again, this wouldn't be very difficult - to support ordinary SIPp's user mode we'll need to create a fixed number of User objects at startup and have them maintain state and own the Call objects. Supporting "pools" would just need an extra UserManager object that kept collections of users and moved them around when requested.

* Multiple scenarios running simultaneously

This could be done by having multiple call-creation tasks, each creating calls using a different scenario and potentially doing so at different rates (e.g. to simulate 10% REGISTER load, 15% MESSAGE load and 75% INVITE/call load).

On the UAS side, the right approach isn't quite as obvious - one approach might be to allow one different scenario per request type (e.g. a MESSAGE scenario, NOTIFY scenario, INVITE scenario and so on) so that incoming initial requests could easily be matched. This wouldn't quite cover the possibility of having multiple distinct INVITE scenarios (for example, one with redirects and one without).

* The ability to "pair" users out of pools to run scenarios

This feature involves - given a pool of registered users - SIPp being able to pick two users, A and B, out of that pool, and have A perform the UAC half of a scenario targeted towards B's SIP URI, and B would perform the UAS half of the same scenario. This would mean that B would know exactly what scenario the caller was using, and so a single SIPp instance could run multiple subtly different scemnarios (ordinary INVITE, INVITE with 183, INVITE with UPDATE, INVITE with CANCEL...) simultaneously. Once pools of users are implemented, this shouldn't be a very complex change to the call generation task.

Overall, the main architectural difference from the existing IMSBench would be that the individual nodes would be a lot more autonomous - the master would be responsible for starting them up and collecting statistics, and might need to propagate changes like a change in call rate out to them, but wouldn't need to correlate calls between them most of the time.

# Better support for 'unpredictable' call events

Some SIP test scenarios involve events that are not entirely predictable - in particular, receiving an in-dialog INVITE or UPDATE to update SDP or act as session refresh. Tests that establish a call and then pause for a random amount of time (to simulate the distribution of real user behaviour) are especially susceptible to receiving an UPDATE mid-pause and failing. It would be useful to have some easy way of handling these (such as a <subscenario> block to handle otherwise-unexpected in-call messages), so that a scenario could define automatic INVITE or UPDATE handling, then pause for a statistically-distributed length of time.
