package dimartinofilippo.agenda.repository.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;

public class ToDoSQLRepository implements ToDoRepository {

	static final String TABLE_NAME = "todos";
	static final String SELECT_ALL = "SELECT title, done FROM " + TABLE_NAME + " ORDER BY title";
	static final String SELECT_BY_TITLE = "SELECT title, done FROM " + TABLE_NAME + " WHERE title = ?";
	static final String INSERT = "INSERT INTO " + TABLE_NAME + "(title, done) VALUES(?,?)";
	static final String DELETE = "DELETE FROM " + TABLE_NAME + " WHERE title = ?";

	private DataSource dataSource;

	public ToDoSQLRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public ToDo save(ToDo todo) {
		String sql = INSERT;
		try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, todo.getTitle());
			ps.setBoolean(2, todo.isDone());
			ps.executeUpdate();

			return todo;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Optional<ToDo> findByTitle(String title) {
		String sql = SELECT_BY_TITLE;
		try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, title);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(new ToDo(rs.getString("title"), rs.getBoolean("done")));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return Optional.empty();
	}

	@Override
	public List<ToDo> findAll() {
		String sql = SELECT_ALL;
		List<ToDo> result = new ArrayList<>();
		try (Connection c = dataSource.getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				result.add(new ToDo(rs.getString("title"), rs.getBoolean("done")));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@Override
	public void deleteByTitle(String title) {
		String sql = DELETE;
		try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, title);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
