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
	
    private static final String TODO_TITLE = "test";
    private static final boolean TODO_DONE = false;

	
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
		ToDo todo = new ToDo(TODO_TITLE, TODO_DONE);
		when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.empty());
		agendaController.addToDo(todo);
		
		InOrder inOrder = Mockito.inOrder(todoRepository, todoView);
		inOrder.verify(todoRepository).save(todo);
		inOrder.verify(todoView).addedToDo(todo);
		
	}
	
	@Test
	public void testAddToDoWhenAlreadyExist() {
		ToDo todoToAdd = new ToDo(TODO_TITLE, TODO_DONE);
		ToDo existingToDo = new ToDo(TODO_TITLE, !TODO_DONE);
		
		when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.of(existingToDo));
		agendaController.addToDo(todoToAdd);
		verify(todoView).showError("same ToDo already in the agenda: " + existingToDo.getTitle());
		verifyNoMoreInteractions(ignoreStubs(todoRepository));
		
	}
	
	@Test
	public void testDeleteToDoWhenExist() {
		ToDo todoToDelete = new ToDo(TODO_TITLE, TODO_DONE);
		
		when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.of(todoToDelete));
		agendaController.deleteToDo(todoToDelete);
		
		InOrder inOrder = Mockito.inOrder(todoRepository, todoView);
		inOrder.verify(todoRepository).deleteByTitle(TODO_TITLE);
		inOrder.verify(todoView).removedToDo(todoToDelete);
		
	}
	
	
	@Test
	public void testDeleteToDoWhenDoesNotExist() {
		ToDo todo = new ToDo(TODO_TITLE, TODO_DONE);
		
		when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.empty());
		agendaController.deleteToDo(todo);
		
		verify(todoView).showError("ToDo doesn't exist: " + todo.getTitle());
		verifyNoMoreInteractions(ignoreStubs(todoRepository));

		
	}

}
