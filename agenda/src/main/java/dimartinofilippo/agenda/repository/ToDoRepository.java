package dimartinofilippo.agenda.repository;

import java.util.List;
import java.util.Optional;

import dimartinofilippo.agenda.model.ToDo;

public interface ToDoRepository {
	ToDo save(ToDo todo);

	Optional<ToDo> findByTitle(String title);

	List<ToDo> findAll();

	void deleteByTitle(String title);
}