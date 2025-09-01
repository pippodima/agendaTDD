package dimartinofilippo.agenda.controller;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.MongoClients;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.view.ToDoView;

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
	


}
