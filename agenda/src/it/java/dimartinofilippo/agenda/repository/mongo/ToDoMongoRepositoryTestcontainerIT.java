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
}
