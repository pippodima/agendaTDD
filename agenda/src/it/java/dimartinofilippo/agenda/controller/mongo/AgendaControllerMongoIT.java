package dimartinofilippo.agenda.controller.mongo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.MongoClients;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.transaction.mongo.MongoTransactionManager;
import dimartinofilippo.agenda.view.ToDoView;




public class AgendaControllerMongoIT {
	
	private ToDoView todoView;
	private ToDoRepository todoRepository;
	private AgendaController agendaController;
	private MongoTransactionManager transactionManager;
	
	@BeforeEach
	void setUp() {
		todoView = mock(ToDoView.class);
		MockitoAnnotations.openMocks(this);
		
		todoRepository = new ToDoMongoRepository(MongoClients.create("mongodb://localhost:27017"));
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
