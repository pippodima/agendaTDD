package dimartinofilippo.agenda.repository.mongo;

import java.net.InetSocketAddress;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class ToDoMongoRepositoryTest {
	
	private static MongoServer server;
	private static InetSocketAddress serverAddress;
	
	private MongoClient client;
	private ToDoMongoRepository todoRepository;
	private MongoCollection<Document> todoCollection;
	
	@BeforeAll
	void setupServer() {
		server = new MongoServer(new MemoryBackend());
		serverAddress = server.bind();
		
	}
	
	@AfterAll 
	void shutdownServer(){
		server.shutdown();
	}
	

}
