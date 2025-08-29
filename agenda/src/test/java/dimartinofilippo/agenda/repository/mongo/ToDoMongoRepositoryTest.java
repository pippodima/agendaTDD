package dimartinofilippo.agenda.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import dimartinofilippo.agenda.model.ToDo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ToDoMongoRepositoryTest {
	
    private static final String TODO_TITLE = "test";
    private static final boolean TODO_DONE = false;
	
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
	
    @BeforeEach
    void setup() {
        String uri = "mongodb://" + serverAddress.getHostName() + ":" + serverAddress.getPort();
        client = MongoClients.create(uri);

        MongoDatabase database = client.getDatabase(ToDoMongoRepository.AGENDA_DB_NAME);
        database.drop();
        
        todoRepository = new ToDoMongoRepository(client);
        todoCollection = database.getCollection(ToDoMongoRepository.TODO_COLLECTION_NAME);
        
        assertThat(todoCollection.countDocuments()).isZero();

    }
    
    @AfterEach
    void tearDownClient() {
    	client.close();
    }
    
    @Test
    public void testFindAllWhenDBIsEmpty() {
    	assertTrue(todoRepository.findAll().isEmpty());
    
    }


}
