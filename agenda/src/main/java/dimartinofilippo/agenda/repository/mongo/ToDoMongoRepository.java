package dimartinofilippo.agenda.repository.mongo;

import java.util.List;
import java.util.Optional;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;

public class ToDoMongoRepository implements ToDoRepository{

	@Override
	public ToDo save(ToDo todo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ToDo> findByTitle(String title) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<ToDo> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteByTitle(String title) {
		// TODO Auto-generated method stub
		
	}

}
