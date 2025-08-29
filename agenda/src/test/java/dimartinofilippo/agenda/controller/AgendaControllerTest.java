package dimartinofilippo.agenda.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.view.ToDoView;

public class AgendaControllerTest {
	
    private static final String TEST_STRING = "test";
    private static final boolean TEST_VALUE = false;

	
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
	
	@Test
	public void testAddToDoWhenDoesNotExist() {
		ToDo todo = new ToDo(TEST_STRING, TEST_VALUE);
		when(todoRepository.findByTitle(TEST_STRING)).thenReturn(Optional.empty());
		agendaController.addToDo(todo);
		
		InOrder inOrder = Mockito.inOrder(todoRepository, todoView);
		inOrder.verify(todoRepository).save(todo);
		inOrder.verify(todoView).addedToDo(todo);
		
	}
	
	@Test
	public void testAddToDoWhenAlreadyExist() {
		ToDo todoToAdd = new ToDo(TEST_STRING, TEST_VALUE);
		ToDo existingToDo = new ToDo(TEST_STRING, !TEST_VALUE);
		
		when(todoRepository.findByTitle(TEST_STRING)).thenReturn(Optional.of(existingToDo));
		agendaController.addToDo(todoToAdd);
		verify(todoView).showError("same ToDo already in the agenda" + existingToDo.getTitle());
		verifyNoMoreInteractions(ignoreStubs(todoRepository));
		
	}
	
	@Test
	public void testDeleteToDoWhenExist() {
		ToDo todoToDelete = new ToDo(TEST_STRING, TEST_VALUE);
		
		when(todoRepository.findByTitle(TEST_STRING)).thenReturn(Optional.of(todoToDelete));
		agendaController.deleteToDoByTitle(TEST_STRING);
		
		InOrder inOrder = Mockito.inOrder(todoRepository, todoView);
		inOrder.verify(todoRepository).deleteByTitle(TEST_STRING);
		inOrder.verify(todoView).removedToDo(todoToDelete);
		
	}

}
