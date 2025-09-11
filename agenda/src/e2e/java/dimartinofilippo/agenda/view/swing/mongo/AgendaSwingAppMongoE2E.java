package dimartinofilippo.agenda.view.swing.mongo;

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
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgendaSwingAppMongoE2E {

	private static final String DB_NAME = "agenda";
	private static final String COLLECTION_NAME = "todos";
	private static final String WINDOW_TITLE = "Agenda - ToDo List";
	private static final int WINDOW_TIMEOUT_MS = 5000;
	private static final int ROBOT_DELAY_MS = 50;

	private static final ToDo TODO_1 = new ToDo("todo1", true);
	private static final ToDo TODO_2 = new ToDo("todo2", false);

	@Container
	static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:4.0.5");

	private Robot robot;
	private MongoClient mongoClient;
	private MongoCollection<Document> todoCollection;
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
	void setUp() {
		setupDatabaseConnection();

		prepareTestData();

		launchApplication();

		setupWindowFixture();
	}

	@AfterEach
	void tearDown() {
		if (window != null) {
			window.cleanUp();
		}
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	// helpers

	private void setupDatabaseConnection() {
		mongoClient = MongoClients.create(mongoContainer.getReplicaSetUrl());
		MongoDatabase database = mongoClient.getDatabase(DB_NAME);
		database.drop(); // Clean slate for each test
		todoCollection = database.getCollection(COLLECTION_NAME);
	}

	private void prepareTestData() {
		addTestTodoToDatabase(TODO_1.getTitle(), TODO_1.isDone());
		addTestTodoToDatabase(TODO_2.getTitle(), TODO_2.isDone());
	}

	private void launchApplication() {
		application("dimartinofilippo.agenda.App").withArgs("mongo", mongoContainer.getHost(),
				String.valueOf(mongoContainer.getMappedPort(27017)), DB_NAME).start();
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
	void shouldShowErrorWhenDeletingNonExistentTodo() {
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

	private void addTestTodoToDatabase(String title, boolean done) {
		todoCollection.insertOne(new Document().append("title", title).append("done", done));
	}

	private void deleteTestTodoFromDatabase(String title) {
		todoCollection.deleteOne(Filters.eq("title", title));
	}
}