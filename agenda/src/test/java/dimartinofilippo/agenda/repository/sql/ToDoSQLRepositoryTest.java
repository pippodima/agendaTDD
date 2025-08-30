package dimartinofilippo.agenda.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import dimartinofilippo.agenda.model.ToDo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ToDoSQLRepositoryTest {

	private DataSource dataSource;
	private ToDoSQLRepository sqlRepository;

	@BeforeAll
	void setupDB() throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:agenda;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds.setUser("pippo");
		ds.setPassword("");
		this.dataSource = ds;
		createSchema();
	}

	@BeforeEach
	void clean() throws Exception {
		try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("TRUNCATE TABLE todos");
		}

		sqlRepository = new ToDoSQLRepository(dataSource);
	}

	@Test
	void findAllWhenEmptyReturnEmptyList() {
		assertThat(sqlRepository.findAll().isEmpty());
	}

	@Test
	void findAllWhenNotEmptyReturnsRows() throws Exception {
		insert("todo1", true);
		insert("todo2", false);
		assertThat(sqlRepository.findAll()).containsExactly(new ToDo("todo1", true), new ToDo("todo2", false));
	}
	
	@Test
	void findByTitle_notFound_returnsEmpty() {
	assertThat(sqlRepository.findByTitle("missing")).isEmpty();
	}

	@Test
	void findByTitle_found_returnsOptional() throws Exception {
	insert("task1", false);
	insert("task2", true);
	assertThat(sqlRepository.findByTitle("task2")).isPresent().contains(new ToDo("task2", true));
	}

	// helpers
	private void createSchema() throws Exception {
		try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS todos (" + "title VARCHAR(255) PRIMARY KEY,"
					+ "done BOOLEAN NOT NULL" + ")");
		}
	}

	private void insert(String title, boolean done) throws Exception {
		try (Connection c = dataSource.getConnection();
				PreparedStatement ps = c.prepareStatement("INSERT INTO todos(title, done) VALUES(?, ?)")) {
			ps.setString(1, title);
			ps.setBoolean(2, done);
			ps.executeUpdate();
		}
	}

}
