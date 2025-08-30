package dimartinofilippo.agenda.repository.mongo;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dimartinofilippo.agenda.model.ToDo;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ToDoMongoRepositoryTestcontainersIT {

    @Container
    public static final GenericContainer<?> mongo =
            new GenericContainer<>("mongo:4.0.5")
                    .withExposedPorts(27017);

    private MongoClient client;
    private ToDoMongoRepository todoRepository;
    private MongoCollection<Document> todoCollection;

    @BeforeEach
    void setup() {
        String uri = "mongodb://" + mongo.getHost() + ":" + mongo.getMappedPort(27017);

        client = MongoClients.create(uri);

        todoRepository = new ToDoMongoRepository(client);
        MongoDatabase database = client.getDatabase(ToDoMongoRepository.AGENDA_DB_NAME);

        database.drop();
        todoCollection = database.getCollection(ToDoMongoRepository.TODO_COLLECTION_NAME);
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void testContainerConnection() {
        assertThat(todoCollection.countDocuments()).isZero();
    }
    
    @Test
    void testFindAll() {
    	addTestToDoToDatabase("todo1", true);
    	addTestToDoToDatabase("todo2", false);
    	
    	assertThat(todoRepository.findAll())
    		.containsExactly(
    				new ToDo("todo1", true),
    				new ToDo("todo2", false)
    				);
    	
    }
    
    @Test
    void testFindByTitle() {
    	addTestToDoToDatabase("todo1", true);
    	addTestToDoToDatabase("todo2", false);

    	assertThat(todoRepository.findByTitle("todo2"))
    		.contains(new ToDo("todo2", false));
    }
    

    
    // helper
    
    private void addTestToDoToDatabase(String title, boolean done) {
        todoCollection.insertOne(
                new Document()
                        .append("title", title)
                        .append("done", done));
    }

    
    
}
