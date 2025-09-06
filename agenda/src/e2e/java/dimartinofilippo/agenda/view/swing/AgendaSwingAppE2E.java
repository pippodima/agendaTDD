package dimartinofilippo.agenda.view.swing;

import dimartinofilippo.agenda.model.ToDo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.*;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.bson.Document;
import org.junit.jupiter.api.*;

import org.testcontainers.containers.GenericContainer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AgendaSwingAppE2E {

    @SuppressWarnings("rawtypes")
    public static final GenericContainer mongo =
            new GenericContainer("mongo:4.0.5").withExposedPorts(27017);

    private static final String DB_NAME = "agenda";
    private static final String COLLECTION_NAME = "todos";

    private static Robot robot;

    private MongoClient mongoClient;
    private FrameFixture window;

    @BeforeAll
    static void setUpRobot() {
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(50);
    }

    @AfterAll
    static void tearDownRobot() {
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @BeforeEach
    void setUp() {
        mongo.start();

        String containerIpAddress = mongo.getHost();
        Integer mappedPort = mongo.getMappedPort(27017);

        mongoClient = MongoClients.create("mongodb://" + containerIpAddress + ":" + mappedPort);
        mongoClient.getDatabase(DB_NAME).drop();

        addTestTodoToDatabase("todo1", true);
        addTestTodoToDatabase("todo2", false);

        application("dimartinofilippo.agenda.App").withArgs("mongo", containerIpAddress, String.valueOf(mappedPort), DB_NAME).start();

        window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
            @Override
            protected boolean isMatching(JFrame frame) {
                return "Agenda - ToDo List".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot);
    }

    @AfterEach
    void tearDown() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        mongo.stop();
        if (window != null) {
            window.cleanUp();
        }
    }

    @Test
    void testOnStartAllDBElementsAreShown() {
        assertThat(window.list().contents())
            .anySatisfy(e -> assertThat(e).contains(new ToDo("todo1", true).toString()))
            .anySatisfy(e -> assertThat(e).contains(new ToDo("todo2", false).toString()));
    }

    // helper
    private void addTestTodoToDatabase(String title, boolean done) {
        mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME)
            .insertOne(new Document().append("title", title).append("done", done));
    }
}
