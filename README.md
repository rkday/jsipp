# JSIPp

## What is this?

This is an experimental rewrite of [SIPp](http://sipp.sourceforge.net) in Java, designed to be compatible with the same XML files and command-line arguments, and have roughly equal performance, but to provide a cleaner, smaller, more modular base for faster future development.

## Why a rewrite in Java?

A lot of function in SIPp - the hashed wheel timer, select/epoll loops, XML parsing - is part of the Java standard library or available in other common libraries like Netty. Being able to rely on standard implementations of these, and focus on the test-tool-specific logic, should make SIPp a lot smaller, more stable and easier to manage (the C++ version is currently at around 29,500 lines f code compared to 850 in this Java version).

Java is also able to roughly match the performance of C++, which is crucial as one of SIPp's main applications is in performance testing.

Also, one of the main issues with SIPp was the lack of good unit test support - Java's unit testing (for example, with JUnit) seems much more mature than the C++ test libraries.

## What's the status?

Version 0.0.1 has all the basic infrastructure (scheduler thread, selector thread, call opening, message send/receive) but no logging, UI, stats, in-call actions, or most keywords (only [call_num] and [call_id] are implemented).

It can do basic "send a SIP MESSAGE, receive a 200 OK" - a working file is in `resources/message.xml`.

It's usable with `java -jar jsipp-0.0.1.jar -sf message.xml -r <rate per second> <server>:<port>`.

## How can I contribute?

See the [design notes](https://github.com/rkday/jsipp/blob/master/design.md) for an overview. Good things to implement are TCP, socket multiplexing or additinal SIPp keywords - a more detailed TODO is coming soon.

Feedback on the [planned future directions](https://github.com/rkday/jsipp/blob/master/future-directions.md) would also be useful, especially if there are use-cases it doesn't cover.
