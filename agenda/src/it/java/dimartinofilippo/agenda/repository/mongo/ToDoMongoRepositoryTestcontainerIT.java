package dimartinofilippo.agenda.repository.mongo;

import org.bson.Document;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dimartinofilippo.agenda.model.ToDo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ToDoMongoRepositoryTestcontainersIT {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:4.0.5");

    private MongoClient client;
    private ToDoMongoRepository todoRepository;
    private MongoCollection<Document> todoCollection;

    @BeforeAll
    void initClient() {
        client = MongoClients.create(mongo.getReplicaSetUrl()); // optimized connection
        MongoDatabase database = client.getDatabase(ToDoMongoRepository.AGENDA_DB_NAME);
        todoCollection = database.getCollection(ToDoMongoRepository.TODO_COLLECTION_NAME);
        todoRepository = new ToDoMongoRepository(client);
    }

    @BeforeEach
    void cleanDatabase() {
        client.getDatabase(ToDoMongoRepository.AGENDA_DB_NAME).drop();
    }

    @AfterAll
    void closeClient() {
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

    @Test
    void testFindByTitleNotFound() {
        assertThat(todoRepository.findByTitle("does-not-exist")).isEmpty();
    }

    @Test
    void testSave() {
        ToDo todo = new ToDo("todo", true);
        todoRepository.save(todo);

        assertThat(readAllTodosFromDatabase())
            .containsExactly(todo);
    }

    @Test
    void testDeleteByTitle() {
        addTestToDoToDatabase("todo1", true);
        todoRepository.deleteByTitle("todo1");

        assertThat(readAllTodosFromDatabase()).isEmpty();
    }

    // helpers

    private void addTestToDoToDatabase(String title, boolean done) {
        todoCollection.insertOne(
            new Document()
                .append("title", title)
                .append("done", done)
        );
    }

    private List<ToDo> readAllTodosFromDatabase() {
        return StreamSupport
            .stream(todoCollection.find().spliterator(), false)
            .map(d -> new ToDo(d.getString("title"), d.getBoolean("done")))
            .collect(Collectors.toList());
    }
}
