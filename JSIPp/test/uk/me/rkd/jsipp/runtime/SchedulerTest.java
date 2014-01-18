package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.*;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import org.junit.Test;

class TestTimer implements TimerTask {
	public int timesScheduled = 0;
	
	public void run(Timeout timer) {
		this.timesScheduled += 1;
	}
}

public class SchedulerTest {

	@Test
	public void test() throws InterruptedException {
		Scheduler s = new Scheduler(100);
		TestTimer t = new TestTimer();
		s.add(t, 0);
		assertEquals(0, t.timesScheduled);
		Thread.sleep(200);
		assertEquals(1, t.timesScheduled);	
	}

	@Test
	public void testStopping() throws InterruptedException {
		Scheduler s = new Scheduler(100);
		TestTimer t = new TestTimer();
		s.add(t, 0);
		s.stop();
		assertEquals(0, t.timesScheduled);
		Thread.sleep(200);
		assertEquals(0, t.timesScheduled);	
	}
	
}
