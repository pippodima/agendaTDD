package dimartinofilippo.agenda.controller.mongo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.transaction.mongo.MongoTransactionManager;
import dimartinofilippo.agenda.view.ToDoView;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AgendaControllerMongoIT {

	@Container
	static final MongoDBContainer mongo = new MongoDBContainer("mongo:4.0.5");

	private ToDoView todoView;
	private ToDoRepository todoRepository;
	private AgendaController agendaController;
	private MongoTransactionManager transactionManager;
	private MongoClient mongoClient;

	@BeforeEach
	void setUp() {
		todoView = mock(ToDoView.class);
		MockitoAnnotations.openMocks(this);

		if (mongoClient == null) {
			mongoClient = MongoClients.create(mongo.getReplicaSetUrl());
		}

		todoRepository = new ToDoMongoRepository(mongoClient);

		for (ToDo todo : todoRepository.findAll()) {
			todoRepository.deleteByTitle(todo.getTitle());
		}

		transactionManager = new MongoTransactionManager(todoRepository);
		agendaController = new AgendaController(transactionManager, todoView);
	}

	@Test
	void testAllToDo() {
		ToDo todo = new ToDo("task1", true);
		transactionManager.doInTransaction(todoRepository -> todoRepository.save(todo));
		agendaController.allToDos();
		verify(todoView).showAllToDos(asList(todo));
	}

	@Test
	void testAddToDo() {
		ToDo todo = new ToDo("task1", true);
		agendaController.addToDo(todo);
		verify(todoView).addedToDo(todo);
	}

	@Test
	void testDeleteToDo() {
		ToDo todo = new ToDo("task1", true);
		transactionManager.doInTransaction(todoRepository -> todoRepository.save(todo));
		agendaController.deleteToDo(todo);
		verify(todoView).removedToDo(todo);
	}

}
