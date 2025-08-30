package dimartinofilippo.agenda.controller;

import java.util.List;
import java.util.Optional;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.view.ToDoView;

public class AgendaController {
	
	private final ToDoRepository todoRepository;
	private final ToDoView todoView;
	
    public AgendaController(ToDoRepository todoRepository, ToDoView todoView) {
        this.todoRepository = todoRepository;
        this.todoView = todoView;
    }

	public List<ToDo> allToDos() {
		List<ToDo> todos = todoRepository.findAll();
		todoView.showAllToDos(todos);
		return todos;
		
	}

	public void addToDo(ToDo todo) {
		Optional<ToDo> existingTodo = todoRepository.findByTitle(todo.getTitle());
		if (existingTodo.isPresent()) {
			todoView.showError("same ToDo already in the agenda: " + existingTodo.get().getTitle());
			return;
		}
		
		todoRepository.save(todo);
		todoView.addedToDo(todo);
		
	}

	public void deleteToDo(ToDo todo) {
		Optional<ToDo> existingTodo = todoRepository.findByTitle(todo.getTitle());

		if(existingTodo.isEmpty()) {
			todoView.showError("ToDo doesn't exist: " + todo.getTitle());
			return;
		}
		
		todoRepository.deleteByTitle(todo.getTitle());
		todoView.removedToDo(todo);
	}

}
