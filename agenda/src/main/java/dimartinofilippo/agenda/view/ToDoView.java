package dimartinofilippo.agenda.view;

import java.util.List;

import dimartinofilippo.agenda.model.ToDo;

public interface ToDoView {

	void showAllToDos(List<ToDo> todos);
	void addedToDo(ToDo todo);
	void removedToDo(ToDo todo);
	void showError(String string);
}
