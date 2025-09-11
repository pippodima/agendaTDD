package dimartinofilippo.agenda.controller.sql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.sql.ToDoSQLRepository;
import dimartinofilippo.agenda.transaction.sql.SQLTransactionManager;
import dimartinofilippo.agenda.view.ToDoView;

public class AgendaControllerSQLIT {

	private ToDoView todoView;
	private ToDoRepository todoRepository;
	private AgendaController agendaController;
	private SQLTransactionManager transactionManager;

	private DataSource dataSource;

	@BeforeEach
	void setUp() throws SQLException {
		todoView = mock(ToDoView.class);
		MockitoAnnotations.openMocks(this);

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("");
		this.dataSource = ds;

		todoRepository = new ToDoSQLRepository(dataSource);

		try (Connection c = dataSource.getConnection(); Statement stmt = c.createStatement()) {
			stmt.execute("DROP TABLE IF EXISTS todos");
			stmt.execute("""
					    CREATE TABLE todos (
					        title VARCHAR(255) PRIMARY KEY,
					        done BOOLEAN
					    )
					""");
		}

		Connection connection = dataSource.getConnection();
		transactionManager = new SQLTransactionManager(todoRepository, connection);

		agendaController = new AgendaController(transactionManager, todoView);
	}

	@Test
	void testAllToDos() {
		ToDo todo = new ToDo("task1", true);
		transactionManager.doInTransaction(todoRepository -> todoRepository.save(todo));

		agendaController.allToDos();

		verify(todoView).showAllToDos(asList(todo));
	}

	@Test
	void testAddToDo() {
		ToDo todo = new ToDo("task1", true);
		agendaController.addToDo(todo);

		verify(todoView).addedToDo(todo);
	}

	@Test
	void testDeleteToDo() {
		ToDo todo = new ToDo("task1", true);
		transactionManager.doInTransaction(todoRepository -> todoRepository.save(todo));

		agendaController.deleteToDo(todo);

		verify(todoView).removedToDo(todo);
	}
}
