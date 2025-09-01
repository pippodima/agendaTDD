package dimartinofilippo.agenda.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.MongoClients;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.view.ToDoView;


/**
 * Communicates with a MongoDB server on localhost; start MongoDB with Docker with
 * 
 * <pre>
 * docker run -p 27017:27017 --rm mongo:4.0.5
 * </pre>
 * 
 */


public class AgendaControllerIT {
	
	private ToDoView todoView;
	private ToDoRepository todoRepository;
	private AgendaController agendaController;
	
	@BeforeEach
	void setUp() {
		todoView = mock(ToDoView.class);
		MockitoAnnotations.openMocks(this);
		
		todoRepository = new ToDoMongoRepository(MongoClients.create("mongodb://localhost:27017"));
		for (ToDo todo : todoRepository.findAll()) {
			todoRepository.deleteByTitle(todo.getTitle());
		}
		
		agendaController = new AgendaController(todoRepository, todoView);
		
	}
	
	@Test
	void testAllToDo() {
		ToDo todo = new ToDo("task1", true);
		todoRepository.save(todo);
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
		todoRepository.save(todo);
		agendaController.deleteToDo(todo);
		verify(todoView).removedToDo(todo);
	}
	

}
