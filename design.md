# SIPp Java

## Compiler phase (Scenario.java)

The uk.me.rkd.jsipp.compiler package focuses on converting the XML given into a Scenario object. A scenario includes several "phases" (recv, send, nop or pause) reflecting the in-call actions to be taken. Each of these phases may have some associated data (e.g. a message to send, an expected SIP method to receive, a pause duration).

Each phase may also include a set of actions to be executed when the phase hapens - including logging, performing regular expression matches on the message received, executing external commands, setting in-call variables, and so on.

This functionality is invoked by the Scenario.fromXmlFile method, and creates a Scenario object with multiple CallPhase instances (actually, subclasses of CallPhase). Each of these CallPhase instances optionally has multiple MessageAction instances (again, subclasses of MessageAction implement specific actions).

Although SIPp scenarios are traditionally written as XML files, it should be possible to define a simpler format such as YAML - as long as this can be parsed and a Scenario object created based on it, no changes to the runtime should be necessary to accommodate this.

The code to parse the command-line options and create a Configuration object also forms part of the compiler phase - options could be given in a different input format, like the XML files used for Seagull.

## Runtime phase

### Components

#### Scheduler (Scheduler.java)

The SIPp scheduler allows "tasks" to be registered with it, passing a timeout, and then calls their "run" method once the timeout has passed. Every call is a scheduler task in SIPp - the scheduler is responsible for waking calls up once the receive timeout or pause duration has passed, at which point they will perform any necessary next steps (message sending, actions, etc.) and rechedule themselves to be called after their next timeout.

Calls make up the majority of tasks on the SIPp scheduler, but there are other one-off tasks, such as the call-opening task (which creates new calls at a constant rate) and the watchdog task (not implemented yet - its role is to constantly reschedule itself and check that t is called at around the expected time, to check whether the scheduler is overloaded and waking tasks too slowly).

#### Selector thread (SocketManager.java, UDPMultiSocketManager.java)

The selector thread handles SIPp's networking. It is based on the Java Selectors API (which automatically uses high-performance kernel features like epoll or kqueue where available).

It has three roles:

* Wait for incoming traffic on the open network sockets; when it arrives, read it and dispatch it to the appropriate call.
* Process a queue of new calls, assigning each of them a network socket and adding it to the selector.
* Process a queue of selector keys relating to terminated calls, and remove them from the selector.

The selector thread is owned by a SocketManager, which is the interface by which calls can add or remove themselves - the SocketManager will put the call or key on the appropriate queue and then call wakeup() to terminate the selector's select() call and make it start processing the queues. All removal/insertion/selection of keys has to be done on the same thread, as the selector's synchronisation means they can't happen simultaneously.

In the future it will have other responsibilities:

* Monitoring the main TCP socket for connection requests and accepting them
* Multiplexing a large number of calls onto fewer sockets - including fairly determining which socket a call should be tied to. Multiplexing all the calls onto one socket (like the -t u1 option) is just a specific case of this function.

#### Call opener (CallOpenerTask.java)

##### UAC

In UAC mode, the call opener runs as a scheduled task. Each time it runs, it calculates how many calls it should have opened (ms since SIPp started times the target rate of calls per ms) and compares that to the count of calls it actually has opened, then opens enough calls to make up the difference.

##### UAS

When UAS calls are implemented, the call opener will not run as a scheduled task - instead, the socket manager will alert the call opener whenever it receives a new message that can't be correlated to an existing call.

#### Calls (Call.java, CallPhase.java, MessageAction.java)

Calls are created by the call-opening task with a unique call number, a set of call actions (from the scenario) and a reference to the socket manager. The call is responsible for communicating with the socket manager and establishing a network connection (and closing it on termination), and the call-opening task is responsible for adding the call to the scheduler.

The call can be woken up in one of two ways:

* By the scheduler. The result depends on what stage the call is currently in:
    * If the call is ready to send a message, it sends it and moves onto the next stage.
    * If the call is waiting to receive a message, and the timeout has not yet passed, it reschedules itself based on the timeout. If the timeout has passed, it ends the call.
    * If the call is in pause mode, and the pause is not over, it reschedules itself based on the pause duration. If the pause duration has passed, it moves onto the next stage.
    * If there are no stages left, the call is marked as successful.

* By the socket manager when a message is received. If the call is waiting to receive a message and the message received matches the expected message, it moves onto the next stage. Otherwise, the call fails.

Each stage (i.e. a message send, receive or pause) has a list of actions associated with it (such as regular expression matching or loging). When a stage is successfully completed, the actions are executed. See the RegexpAction object for an example.

Architecturally, the Call object itself is responsible for sending messages, matching incoming messages and rescheduling itself. However, each action executed (regex matching, logging) is represented by its own object, and this object is responsible for performing the action (having been passed a reference to the Call object so tha it can modify call variables and, if applicable, a reference to the received message). The difference in architecture here is because the actions are more numerous and complex than the call phases (which are basically limited to send/receive/pause), so it makes more sense to delegate that behaviour.

Other features (optional receives, the ability to ignore messages received during a pause, etc.) will be supported and documented here in due course.

