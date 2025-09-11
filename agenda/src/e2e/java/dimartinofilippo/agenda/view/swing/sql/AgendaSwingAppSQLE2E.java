package dimartinofilippo.agenda.view.swing.sql;

import dimartinofilippo.agenda.model.ToDo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.*;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgendaSwingAppSQLE2E {

	private static final String DB_NAME = "agenda";
	private static final String USER = "postgres";
	private static final String PASSWORD = "password";
	private static final String WINDOW_TITLE = "Agenda - ToDo List";
	private static final int WINDOW_TIMEOUT_MS = 5000;
	private static final int ROBOT_DELAY_MS = 50;

	private static final ToDo TODO_1 = new ToDo("todo1", true);
	private static final ToDo TODO_2 = new ToDo("todo2", false);

	@SuppressWarnings("resource")
	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14").withDatabaseName(DB_NAME)
			.withUsername(USER).withPassword(PASSWORD);

	private Robot robot;
	private Connection connection;
	private FrameFixture window;

	@BeforeAll
	void globalSetUp() {
		robot = BasicRobot.robotWithNewAwtHierarchy();
		robot.settings().delayBetweenEvents(ROBOT_DELAY_MS);
	}

	@AfterAll
	void globalTearDown() {
		if (robot != null) {
			robot.cleanUp();
		}
	}

	@BeforeEach
	void setUp() throws Exception {
		setupDatabaseConnection();
		prepareDatabaseSchema();
		prepareTestData();
		launchApplication();
		setupWindowFixture();
	}

	@AfterEach
	void tearDown() throws Exception {
		if (window != null) {
			window.cleanUp();
		}
		if (connection != null) {
			connection.close();
		}
	}

	// helpers

	private void setupDatabaseConnection() throws Exception {
		connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
	}

	private void prepareDatabaseSchema() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate("DROP TABLE IF EXISTS todos");
			stmt.executeUpdate("CREATE TABLE todos (" + "id SERIAL PRIMARY KEY, "
					+ "title VARCHAR(255) UNIQUE NOT NULL, " + "done BOOLEAN NOT NULL)");
		}
	}

	private void prepareTestData() throws Exception {
		addTestTodoToDatabase(TODO_1.getTitle(), TODO_1.isDone());
		addTestTodoToDatabase(TODO_2.getTitle(), TODO_2.isDone());
	}

	private void launchApplication() {
		application("dimartinofilippo.agenda.App")
				.withArgs("sql", postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()).start();
	}

	private void setupWindowFixture() {
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return WINDOW_TITLE.equals(frame.getTitle()) && frame.isShowing();
			}
		}).withTimeout(WINDOW_TIMEOUT_MS).using(robot);
	}

	@Test
	@GUITest
	void shouldDisplayAllDatabaseElementsOnStart() {
		assertThat(window.list("todoList").contents()).anySatisfy(item -> assertThat(item).contains(TODO_1.toString()))
				.anySatisfy(item -> assertThat(item).contains(TODO_2.toString()));
	}

	@Test
	@GUITest
	void shouldAddNewTodoSuccessfully() {
		String newTodoTitle = "addButtonSuccess";

		window.textBox("titleTextBox").enterText(newTodoTitle);
		window.checkBox("doneCheckBox").uncheck();
		window.button("addButton").click();

		assertThat(window.list("todoList").contents())
				.anySatisfy(item -> assertThat(item).contains(new ToDo(newTodoTitle, false).toString()));
	}

	@Test
	@GUITest
	void shouldShowErrorWhenAddingExistingTodo() {
		window.textBox("titleTextBox").enterText(TODO_2.getTitle());
		window.checkBox("doneCheckBox").uncheck();
		window.button("addButton").click();

		assertThat(window.list("todoList").contents()).containsExactly(TODO_1.toString(), TODO_2.toString());

		window.label("errorMessageLabel").requireText("same ToDo already in the agenda: " + TODO_2.getTitle());
	}

	@Test
	@GUITest
	void shouldShowErrorWhenAddingTodoWithoutTitle() {
		window.checkBox("doneCheckBox").uncheck();
		window.button("addButton").click();

		assertThat(window.list("todoList").contents()).containsExactly(TODO_1.toString(), TODO_2.toString());

		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	@GUITest
	void shouldDeleteSelectedTodoSuccessfully() {
		window.list("todoList").selectItem(0);
		window.button("deleteButton").click();

		assertThat(window.list("todoList").contents()).containsExactly(TODO_2.toString());
	}

	@Test
	@GUITest
	void shouldShowErrorWhenDeletingNonExistentTodo() throws Exception {
		window.list("todoList").selectItem(0);
		deleteTestTodoFromDatabase(TODO_1.getTitle());

		window.button("deleteButton").click();

		window.label("errorMessageLabel").requireText("ToDo doesn't exist: " + TODO_1.getTitle());
	}

	@Test
	@GUITest
	void shouldShowErrorWhenNoTodoSelected() {
		window.button("deleteButton").click();

		window.label("errorMessageLabel").requireText(" ");
	}

	// db helpers

	private void addTestTodoToDatabase(String title, boolean done) throws Exception {
		try (PreparedStatement ps = connection.prepareStatement("INSERT INTO todos (title, done) VALUES (?, ?)")) {
			ps.setString(1, title);
			ps.setBoolean(2, done);
			ps.executeUpdate();
		}
	}

	private void deleteTestTodoFromDatabase(String title) throws Exception {
		try (PreparedStatement ps = connection.prepareStatement("DELETE FROM todos WHERE title = ?")) {
			ps.setString(1, title);
			ps.executeUpdate();
		}
	}
}
