package dimartinofilippo.agenda.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.view.ToDoView;

public class AgendaControllerTest {
	
	@Mock
	private ToDoRepository todoRepository;
	
	@Mock
	private ToDoView todoView;
	
	@InjectMocks
	private AgendaController agendaController;
	
	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void testAllToDos() {
		List<ToDo> todos = Arrays.asList(new ToDo());
		when(todoRepository.findAll()).thenReturn(todos);
		List<ToDo> result = agendaController.allToDos();
		assertEquals(todos, result);
		verify(todoView).showAllToDos(todos);		
	}

}
