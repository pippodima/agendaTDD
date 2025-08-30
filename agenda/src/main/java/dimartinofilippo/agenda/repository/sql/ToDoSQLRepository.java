package dimartinofilippo.agenda.repository.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;

public class ToDoSQLRepository implements ToDoRepository {

	private DataSource dataSource;
	
	public ToDoSQLRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

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
	public List<ToDo> findAll() {		// TODO Auto-generated method stub
		return new ArrayList<ToDo>();
	}

	@Override
	public void deleteByTitle(String title) {
		// TODO Auto-generated method stub

	}

}
