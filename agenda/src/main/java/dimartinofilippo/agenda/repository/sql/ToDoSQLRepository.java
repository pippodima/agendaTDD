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

    // Custom exceptions as nested classes
    public static class RepositoryException extends RuntimeException {
        private static final long serialVersionUID = 1L;

		public RepositoryException(String message) {
            super(message);
        }
        
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    public static class DataAccessException extends RepositoryException {
        private static final long serialVersionUID = 2L;

		public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class EntityNotFoundException extends RepositoryException {
        private static final long serialVersionUID = 3L;

		public EntityNotFoundException(String message) {
            super(message);
        }
    }

    // Repository constants and implementation
    static final String TABLE_NAME = "todos";
    static final String SELECT_ALL = "SELECT title, done FROM " + TABLE_NAME + " ORDER BY title";
    static final String SELECT_BY_TITLE = "SELECT title, done FROM " + TABLE_NAME + " WHERE title = ?";
    static final String INSERT = "INSERT INTO " + TABLE_NAME + "(title, done) VALUES(?,?)";
    static final String UPDATE = "UPDATE " + TABLE_NAME + " SET done = ? WHERE title = ?";
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
            throw new DataAccessException("Failed to save todo with title: " + todo.getTitle(), e);
        }
    }

    @Override
    public Optional<ToDo> findByTitle(String title) {
        String sql = SELECT_BY_TITLE;
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(new ToDo(rs.getString("title"), rs.getBoolean("done"))) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find todo with title: " + title, e);
        }
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
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve all todos", e);
        }
    }

    @Override
    public void deleteByTitle(String title) {
        String sql = DELETE;
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Todo with title '" + title + "' not found for deletion");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete todo with title: " + title, e);
        }
    }

}