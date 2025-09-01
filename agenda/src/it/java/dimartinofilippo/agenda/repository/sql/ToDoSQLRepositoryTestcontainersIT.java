package dimartinofilippo.agenda.repository.sql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dimartinofilippo.agenda.model.ToDo;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ToDoSqlRepositoryTestcontainersIT {

    @SuppressWarnings("resource")
	@Container
    public static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("agenda")
                    .withUsername("test")
                    .withPassword("test");

    private DataSource dataSource;
    private ToDoSQLRepository todoRepository;

    @BeforeEach
    void setup() throws Exception {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(postgres.getJdbcUrl());
        ds.setUser(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        this.dataSource = ds;

        try (Connection c = dataSource.getConnection(); Statement stmt = c.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS todos");
            stmt.execute("""
                    CREATE TABLE todos (
                        title VARCHAR(255) PRIMARY KEY,
                        done BOOLEAN NOT NULL
                    )
                    """);
        }

        todoRepository = new ToDoSQLRepository(dataSource);
    }

    @AfterEach
    void tearDown() throws Exception {
        // nothing to close: DataSource manages connections
    }

    @Test
    void testContainerConnection() throws Exception {
        assertThat(readAllTodosFromDatabase()).isEmpty();
    }

    @Test
    void testFindAll() throws Exception {
        addTestToDoToDatabase("todo1", true);
        addTestToDoToDatabase("todo2", false);

        assertThat(todoRepository.findAll())
                .containsExactly(
                        new ToDo("todo1", true),
                        new ToDo("todo2", false)
                );
    }

    @Test
    void testFindByTitle() throws Exception {
        addTestToDoToDatabase("todo1", true);
        addTestToDoToDatabase("todo2", false);

        assertThat(todoRepository.findByTitle("todo2"))
                .contains(new ToDo("todo2", false));
    }

    @Test
    void testSave() throws Exception {
        ToDo todo = new ToDo("todo", true);
        todoRepository.save(todo);

        assertThat(readAllTodosFromDatabase())
                .containsExactly(todo);
    }

    @Test
    void testDeleteByTitle() throws Exception {
        addTestToDoToDatabase("todo1", true);
        todoRepository.deleteByTitle("todo1");

        assertThat(readAllTodosFromDatabase()).isEmpty();
    }

    // helpers

    private void addTestToDoToDatabase(String title, boolean done) throws Exception {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO todos(title, done) VALUES (?, ?)")) {
            ps.setString(1, title);
            ps.setBoolean(2, done);
            ps.executeUpdate();
        }
    }

    private List<ToDo> readAllTodosFromDatabase() throws Exception {
        List<ToDo> todos = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title, done FROM todos ORDER BY title")) {
            while (rs.next()) {
                todos.add(new ToDo(
                        rs.getString("title"),
                        rs.getBoolean("done")
                ));
            }
        }
        return todos;
    }
}
