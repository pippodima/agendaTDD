package dimartinofilippo.agenda.view.swing;

import java.net.InetSocketAddress;

import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class ToDoSwingViewIT extends AssertJSwingJUnitTestCase{
	
	private static MongoServer server;
	private static InetSocketAddress serverAddress;

	@BeforeAll
	static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		serverAddress = server.bind();
	}
	
	@AfterAll
	static void shutDownServer() {
		server.shutdown();
	}

	@Override
	protected void onSetUp() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
