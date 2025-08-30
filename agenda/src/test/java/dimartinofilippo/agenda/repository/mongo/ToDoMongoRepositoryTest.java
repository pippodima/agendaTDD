package dimartinofilippo.agenda.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    	assertThat(todoRepository.findAll().isEmpty());
    }
    
    @Test
    public void testFindAllWhenDBIsNotEmpty() {
    	addTestToDoToDatabase("todo1", true);
    	addTestToDoToDatabase("todo2", false);
    	
    	assertThat(todoRepository.findAll()).containsExactly(
    			new ToDo("todo1", true),
    			new ToDo("todo2", false)
    			);
    	
    }
    
    
    @Test
    void testFindByTitleNotFound() {
        assertThat(todoRepository.findByTitle("nonexistent")).isEmpty();
    }

    @Test
    void testFindByTitleFound() {
        addTestToDoToDatabase("task1", false);
        addTestToDoToDatabase("task2", true);

        assertThat(todoRepository.findByTitle("task2"))
            .isPresent()
            .contains(new ToDo("task2", true));
    }

    
    @Test
    void testSave() {
        ToDo todo = new ToDo("task1", false);
        todoRepository.save(todo);

        assertThat(readAllToDosFromDatabase())
            .containsExactly(todo);
    }

    @Test
    void testDelete() {
        addTestToDoToDatabase("task1", false);
        todoRepository.deleteByTitle("task1");

        assertThat(readAllToDosFromDatabase())
            .isEmpty();
    }
    

    // helpers
    private void addTestToDoToDatabase(String title, boolean done) {
        todoCollection.insertOne(
            new Document()
                .append("title", title)
                .append("done", done)
        );
    }
    
    private List<ToDo> readAllToDosFromDatabase() {
        return StreamSupport.stream(todoCollection.find().spliterator(), false)
                .map(d -> new ToDo(d.getString("title"), d.getBoolean("done", false)))
                .collect(Collectors.toList());
    }



}