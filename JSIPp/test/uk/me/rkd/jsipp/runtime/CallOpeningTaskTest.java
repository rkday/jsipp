package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Any;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.network.SocketManager;

public class CallOpeningTaskTest {

	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException, InterruptedException {
		SocketManager sm = mock(SocketManager.class);
		Scenario s = Scenario.fromXMLFilename("resources/message-uas.xml");
		Scheduler sched = new Scheduler(1);
		CallOpeningTask task = new CallOpeningTask(s, sm, 100);
		sched.add(task, 0);
		Thread.sleep(50);
		verify(sm, atLeastOnce()).add(Matchers.any(Call.class));	
		task.stop();
	}

}
