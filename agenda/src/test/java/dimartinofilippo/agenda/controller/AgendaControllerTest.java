package dimartinofilippo.agenda.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.transaction.TransactionManager;
import dimartinofilippo.agenda.view.ToDoView;

@ExtendWith(MockitoExtension.class)
class AgendaControllerTest {

    private static final String TODO_TITLE = "test";
    private static final boolean TODO_DONE = false;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private ToDoRepository todoRepository;

    @Mock
    private ToDoView todoView;

    @InjectMocks
    private AgendaController agendaController;

    @BeforeEach
    void setUp() {
        when(transactionManager.doInTransaction(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            dimartinofilippo.agenda.transaction.TransactionCode<Object> code =
                    invocation.getArgument(0, dimartinofilippo.agenda.transaction.TransactionCode.class);
            return code.apply(todoRepository);
        });
    }

    @Test
    void testAllToDos() {
        List<ToDo> todos = Arrays.asList(new ToDo());
        when(todoRepository.findAll()).thenReturn(todos);

        List<ToDo> result = agendaController.allToDos();

        assertEquals(todos, result);
        verify(todoView).showAllToDos(todos);
    }

    @Test
    void testAddToDoWhenDoesNotExist() {
        ToDo todo = new ToDo(TODO_TITLE, TODO_DONE);
        when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.empty());

        agendaController.addToDo(todo);

        var inOrder = inOrder(todoRepository, todoView);
        inOrder.verify(todoRepository).save(todo);
        inOrder.verify(todoView).addedToDo(todo);
    }

    @Test
    void testAddToDoWhenAlreadyExist() {
        ToDo todoToAdd = new ToDo(TODO_TITLE, TODO_DONE);
        ToDo existingToDo = new ToDo(TODO_TITLE, !TODO_DONE);

        when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.of(existingToDo));

        agendaController.addToDo(todoToAdd);

        verify(todoRepository).findByTitle(TODO_TITLE);
        verify(todoView).showError("same ToDo already in the agenda: " + existingToDo.getTitle());
        verifyNoMoreInteractions(todoRepository, todoView);
    }

    @Test
    void testDeleteToDoWhenExist() {
        ToDo todoToDelete = new ToDo(TODO_TITLE, TODO_DONE);
        when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.of(todoToDelete));

        agendaController.deleteToDo(todoToDelete);

        var inOrder = inOrder(todoRepository, todoView);
        inOrder.verify(todoRepository).deleteByTitle(TODO_TITLE);
        inOrder.verify(todoView).removedToDo(todoToDelete);
    }

    @Test
    void testDeleteToDoWhenDoesNotExist() {
        ToDo todo = new ToDo(TODO_TITLE, TODO_DONE);
        when(todoRepository.findByTitle(TODO_TITLE)).thenReturn(Optional.empty());

        agendaController.deleteToDo(todo);

        verify(todoRepository).findByTitle(TODO_TITLE);
        verify(todoView).showError("ToDo doesn't exist: " + todo.getTitle());
        verifyNoMoreInteractions(todoRepository, todoView);
    }
}
