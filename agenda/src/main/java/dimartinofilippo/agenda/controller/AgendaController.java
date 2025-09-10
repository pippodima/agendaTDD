package dimartinofilippo.agenda.controller;

import java.util.List;
import java.util.Optional;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.transaction.TransactionManager;
import dimartinofilippo.agenda.view.ToDoView;

public class AgendaController {

	private final ToDoView todoView;
	private final TransactionManager transactionManager;

	public AgendaController(TransactionManager transactionManager, ToDoView todoView) {
		this.transactionManager = transactionManager;
		this.todoView = todoView;
	}

	public List<ToDo> allToDos() {
		List<ToDo> todos = transactionManager.doInTransaction(todoRepository -> todoRepository.findAll());
		todoView.showAllToDos(todos);
		return todos;

	}

	public void addToDo(ToDo todo) {
		Optional<ToDo> existingTodo = transactionManager
				.doInTransaction(todoRepository -> todoRepository.findByTitle(todo.getTitle()));
		if (existingTodo.isPresent()) {
			todoView.showError("same ToDo already in the agenda: " + existingTodo.get().getTitle());
			return;
		}

		ToDo savedTodo = transactionManager.doInTransaction(todoRepository -> todoRepository.save(todo));
		todoView.addedToDo(savedTodo);

	}

	public void deleteToDo(ToDo todo) {
		Optional<ToDo> existingTodo = transactionManager
				.doInTransaction(todoRepository -> todoRepository.findByTitle(todo.getTitle()));

		if (existingTodo.isEmpty()) {
			todoView.showError("ToDo doesn't exist: " + todo.getTitle());
			return;
		}

		transactionManager.<Void>doInTransaction(todoRepository -> {
			todoRepository.deleteByTitle(todo.getTitle());
			return null;
		});
		todoView.removedToDo(todo);
	}

}
