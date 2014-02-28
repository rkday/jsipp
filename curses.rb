#!/usr/bin/ruby

require 'curses'
require 'ffi-rzmq'
require 'enumerator'

POSITIONS = "%15s %8s %8s %12s %12s %12s\n"
POSITIONS2 = "%-38s %-38s\n"
POSITIONS3 = "%15s %-12s%%-10s%%-10s%%-10s%%-15s\n"

OUT_ARROW = "-------->"
IN_ARROW = "<--------"
PAUSE =    "[%6dms]"

class MinuteLongBuffer
  def initialize
    @values = []
  end
  
  def add v
    @values << [Time.new.to_f, v]
  end
  
  def count
    @values = @values.keep_if {|v| v[0] > (Time.new.to_f - 1)}
    @values.length
  end
end

class Scenario
  def parse_scenario_desc desc
    @strings = []
    @msg_counts = []
    parts = desc.split(":")
    parts.each_slice(2) do |inout, name|
      if inout == "IN"
        @strings << (POSITIONS3 % [name, IN_ARROW])
      else
        @strings << (POSITIONS3 % [name, OUT_ARROW])
      end
      @msg_counts << 0
      @unexpected_msg_counts << 0
      @timeout_counts << 0
    end
  end
  
  def inc_msg idx
    @msg_counts[idx.to_i] += 1
    if idx.to_i == 0
      @new_calls.add 1
    end
  end
  
  def inc_unexpected idx
    @unexpected_msg_counts[idx.to_i] += 1
  end
  
  def inc_timeout idx
    @timeout_counts[idx.to_i] += 1
  end
  
  def update
    Curses.clear
    Curses.addstr(POSITIONS % ["Call-rate", "Length", "Port", "Total-time", "Total-calls", "Remote-host"])
    Curses.addstr(POSITIONS % [("%dcps" % @new_calls.count), "0 ms", "5061", ("%.2fs" % (Time.new.to_f - @start)), "40", "127.0.0.1:5060 (UDP)"])
    Curses.addstr("\n")
    Curses.addstr(POSITIONS2 % ["10 new calls during 1.000s period", "16ms scheduler resolution"])
    Curses.addstr(POSITIONS2 % ["0 concurrent calls (limit 30)", "Peak was 1 calls, after 0s"])
    Curses.addstr(POSITIONS2 % ["0 out-of-call msg (discarded)", "1 open sockets"])
    Curses.addstr("\n")
    Curses.addstr((POSITIONS3 % ["", "",]) % ["Messages", "Retrans", "Timeout", "Unexpected-Msg"])
    @strings.each_with_index do |s, i|
      Curses.addstr(s % [@msg_counts[i], 0, @timeout_counts[i], @unexpected_msg_counts[i]])
    end
    Curses.refresh
  end
  
  def initialize
    @new_calls = MinuteLongBuffer.new
    @strings = []
    @msg_counts = []
    @unexpected_msg_counts = []
    @timeout_counts = []
    @start = Time.new.to_f
    
    Thread.new do
      context = ZMQ::Context.new
      socket = context.socket(ZMQ::SUB)
      socket.connect("tcp://localhost:5556")
      socket.setsockopt(ZMQ::SUBSCRIBE, "SIPP")

      socket2 = context.socket(ZMQ::REQ)
      socket2.connect("tcp://localhost:5557")
      socket2.send_string "",0
      socket2.recv_string msg2 = ""
      parse_scenario_desc msg2
      loop do
        socket.recv_strings(msgs = [])
        msgs.each do |msg|
        name, idx = msg.split(":")
        if name == "SIPP-MSG_RECVD" or name == "SIPP-MSG_SENT"
          inc_msg idx
        end
        if name == "SIPP-UNEXPECTED_MSG_RECVD"
          inc_unexpected idx
        end
        if name == "SIPP-RECV_TIMED_OUT"
          inc_timeout idx
        end
      end
      end
    end
  end   
end

SCENARIO = Scenario.new

Curses.init_screen()

Thread.new do
  loop do
    tmp = Curses.getch
  end
end

loop do
  SCENARIO.update
  sleep 1
end