package org.drools.task.service.mina;

import org.drools.SystemEventListenerFactory;
import org.drools.task.service.TaskClient;
import org.drools.task.service.TaskServiceEscalationBaseTest;
import org.drools.task.service.mina.MinaTaskClientConnector;
import org.drools.task.service.mina.MinaTaskServer;
import org.drools.task.service.mina.MinaTaskClientHandler;

public class TaskServiceEscalationMinaTest extends TaskServiceEscalationBaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		server = new MinaTaskServer(taskService);
		Thread thread = new Thread(server);
		thread.start();
		Thread.sleep(500);

		client = new TaskClient(new MinaTaskClientConnector("client 1",
								new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
		client.connect("127.0.0.1", 9123);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		client.disconnect();
		server.stop();
	}

}
