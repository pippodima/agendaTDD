package dimartinofilippo.agenda.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dimartinofilippo.agenda.model.ToDo;

@ExtendWith(MockitoExtension.class)
class ToDoSQLRepositoryTest {

	@Mock
	private DataSource dataSource;

	@Mock
	private Connection connection;

	@Mock
	private PreparedStatement preparedStatement;

	@Mock
	private Statement statement;

	@Mock
	private ResultSet resultSet;

	@InjectMocks
	private ToDoSQLRepository sqlRepository;

	@BeforeEach
	void setup() throws Exception {
		when(dataSource.getConnection()).thenReturn(connection);
	}

	@Test
	void testFindAllWhenEmptyReturnEmptyList() throws Exception {
		when(connection.prepareStatement(ToDoSQLRepository.SELECT_ALL)).thenReturn(preparedStatement);
		when(preparedStatement.executeQuery()).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(false);

		List<ToDo> result = sqlRepository.findAll();

		assertThat(result).isEmpty();
	}

	@Test
	void testFindAllWhenNotEmptyReturnsRows() throws Exception {
		when(connection.prepareStatement(ToDoSQLRepository.SELECT_ALL)).thenReturn(preparedStatement);
		when(preparedStatement.executeQuery()).thenReturn(resultSet);

		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getString("title")).thenReturn("todo1", "todo2");
		when(resultSet.getBoolean("done")).thenReturn(true, false);

		List<ToDo> result = sqlRepository.findAll();

		assertThat(result).containsExactly(new ToDo("todo1", true), new ToDo("todo2", false));
	}

	@Test
	void testFindByTitle_notFound_returnsEmpty() throws Exception {
		when(connection.prepareStatement(ToDoSQLRepository.SELECT_BY_TITLE)).thenReturn(preparedStatement);
		when(preparedStatement.executeQuery()).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(false);

		Optional<ToDo> result = sqlRepository.findByTitle("missing");

		assertThat(result).isEmpty();
	}

	@Test
	void testFindByTitle_found_returnsOptional() throws Exception {
		when(connection.prepareStatement(ToDoSQLRepository.SELECT_BY_TITLE)).thenReturn(preparedStatement);
		when(preparedStatement.executeQuery()).thenReturn(resultSet);

		when(resultSet.next()).thenReturn(true);
		when(resultSet.getString("title")).thenReturn("task2");
		when(resultSet.getBoolean("done")).thenReturn(true);

		Optional<ToDo> result = sqlRepository.findByTitle("task2");

		assertThat(result).isPresent().contains(new ToDo("task2", true));
		verify(preparedStatement).setString(1, "task2");
	}

	@Test
	void testSaveInsertRow() throws Exception {
		ToDo todo = new ToDo("todo1", false);

		when(connection.prepareStatement(ToDoSQLRepository.INSERT)).thenReturn(preparedStatement);

		sqlRepository.save(todo);

		verify(preparedStatement).setString(1, "todo1");
		verify(preparedStatement).setBoolean(2, false);
		verify(preparedStatement).executeUpdate();
	}

	@Test
	void testDeleteByTitle_removesRow() throws Exception {
		when(connection.prepareStatement(ToDoSQLRepository.DELETE)).thenReturn(preparedStatement);
		when(preparedStatement.executeUpdate()).thenReturn(1);
		sqlRepository.deleteByTitle("task1");

		verify(preparedStatement).setString(1, "task1");
		verify(preparedStatement).executeUpdate();
	}

	@Test
	void testSave_whenSQLException_thenWrapsInDataAccessException() throws Exception {
		when(connection.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

		ToDo todo = new ToDo("x", true);

		assertThatThrownBy(() -> sqlRepository.save(todo))
				.isInstanceOf(ToDoSQLRepository.DataAccessException.class)
				.hasMessageContaining("Failed to save todo with title: x")
				.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	void testFindByTitle_whenSQLException_thenWrapsInDataAccessException() throws Exception {
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
		when(preparedStatement.executeQuery()).thenThrow(new SQLException("fail"));

		assertThatThrownBy(() -> sqlRepository.findByTitle("test"))
				.isInstanceOf(ToDoSQLRepository.DataAccessException.class)
				.hasMessageContaining("Failed to find todo with title: test")
				.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	void testFindAll_whenSQLException_thenWrapsInDataAccessException() throws Exception {
		when(connection.prepareStatement(anyString())).thenThrow(new SQLException("nope"));

		assertThatThrownBy(() -> sqlRepository.findAll())
				.isInstanceOf(ToDoSQLRepository.DataAccessException.class)
				.hasMessageContaining("Failed to retrieve all todos")
				.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	void testDeleteByTitle_whenSQLException_thenWrapsInDataAccessException() throws Exception {
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
		doThrow(new SQLException("bad delete")).when(preparedStatement).executeUpdate();

		assertThatThrownBy(() -> sqlRepository.deleteByTitle("test"))
				.isInstanceOf(ToDoSQLRepository.DataAccessException.class)
				.hasMessageContaining("Failed to delete todo with title: test")
				.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	void testDeleteByTitle_whenEntityNotFound_thenThrowsEntityNotFoundException() throws Exception {
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
		when(preparedStatement.executeUpdate()).thenReturn(0); // No rows affected

		assertThatThrownBy(() -> sqlRepository.deleteByTitle("nonexistent"))
				.isInstanceOf(ToDoSQLRepository.EntityNotFoundException.class)
				.hasMessageContaining("Todo with title 'nonexistent' not found for deletion");
	}

	@Test
	void testUpdate_whenEntityNotFound_thenThrowsEntityNotFoundException() throws Exception {
		ToDo todo = new ToDo("nonexistent", true);
		when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
		when(preparedStatement.executeUpdate()).thenReturn(0); // No rows affected

		assertThatThrownBy(() -> sqlRepository.update(todo))
				.isInstanceOf(ToDoSQLRepository.EntityNotFoundException.class)
				.hasMessageContaining("Todo with title 'nonexistent' not found for update");
	}
}