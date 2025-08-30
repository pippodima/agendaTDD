package dimartinofilippo.agenda.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	// helper
	private void createSchema() throws Exception {
		try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS todos (" + "title VARCHAR(255) PRIMARY KEY,"
					+ "done BOOLEAN NOT NULL" + ")");
		}
	}

}
