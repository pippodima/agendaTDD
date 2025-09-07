package dimartinofilippo.agenda.view.swing.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.transaction.mongo.MongoTransactionManager;
import dimartinofilippo.agenda.view.swing.ToDoSwingView;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ToDoSwingViewMongoIT {
    
    private static MongoServer server;
    private static InetSocketAddress serverAddress;
    
    private MongoClient mongoClient;
    private FrameFixture window;
    private ToDoSwingView todoSwingView;
    private AgendaController agendaController;
    private ToDoMongoRepository todoRepository;
    private MongoTransactionManager transactionManager;
	private Robot robot;
    
    @BeforeAll
    void setupServer() {
        server = new MongoServer(new MemoryBackend());
        serverAddress = server.bind();
    }
    
    @AfterAll
    void shutdownServer() {
        server.shutdown();
    }
    
    @BeforeEach
    void setUp() {
        String uri = "mongodb://" + serverAddress.getHostName() + ":" + serverAddress.getPort();
        mongoClient = MongoClients.create(uri);
        
        todoRepository = new ToDoMongoRepository(mongoClient);
        transactionManager = new MongoTransactionManager(todoRepository);

        
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(50); 
        
        List<ToDo> todos = transactionManager.doInTransaction(repo -> repo.findAll());
        for (ToDo todo : todos) {
            transactionManager.<Void>doInTransaction(repo -> {
                repo.deleteByTitle(todo.getTitle());
                return null;
            });
        }
        
        todoSwingView = GuiActionRunner.execute(() -> {
            ToDoSwingView view = new ToDoSwingView();
            return view;
        });
        
        agendaController = new AgendaController(transactionManager, todoSwingView);
        todoSwingView.setAgendaController(agendaController);
        
        window = new FrameFixture(robot, todoSwingView);
        window.show();
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
    
	@Test
	@GUITest
	void testAllToDos() {
		ToDo todo1 = new ToDo("Buy groceries", false);
		ToDo todo2 = new ToDo("Complete project", true);
		transactionManager.<Void>doInTransaction(todoRepository -> {
			todoRepository.save(todo1);
			todoRepository.save(todo2);
			return null;
		});

		GuiActionRunner.execute(() -> agendaController.allToDos());

		List<String> listContents = List.of(window.list("todoList").contents());
		assertThat(listContents).containsExactly(todo1.toString(), todo2.toString());
	}
    
    @Test
    @GUITest
    void testAddButtonSuccess() {
        window.textBox("titleTextBox").enterText("Learn Java");
        window.checkBox("doneCheckBox").uncheck();
        window.button("addButton").click();
        
        String[] listContents = window.list("todoList").contents();
        assertThat(listContents).containsExactly(new ToDo("Learn Java", false).toString());
    }
    
    @Test
    @GUITest
    void testAddButtonError() {
        transactionManager.doInTransaction(todoRepository -> todoRepository.save(new ToDo("Existing Task", true)));
        
        window.textBox("titleTextBox").enterText("Existing Task");
        window.checkBox("doneCheckBox").uncheck();
        window.button("addButton").click();
        
        assertThat(window.list("todoList").contents()).isEmpty();
        
        window.label("errorMessageLabel")
            .requireText("same ToDo already in the agenda: Existing Task");
    }
    
    @Test
    @GUITest
    void testDeleteButtonSuccess() {
        GuiActionRunner.execute(() -> agendaController.addToDo(new ToDo("Task to remove", false)));
        
        window.list("todoList").selectItem(0);
        window.button("deleteButton").click();
        
        assertThat(window.list("todoList").contents()).isEmpty();
    }
    
    @Test
    @GUITest
    void testDeleteButtonError() {
        ToDo nonExistentTodo = new ToDo("Non-existent task", false);
        GuiActionRunner.execute(() -> todoSwingView.getListTodosModel().addElement(nonExistentTodo));
        
        window.list("todoList").selectItem(0);
        window.button("deleteButton").click();
        
        String[] listContents = window.list("todoList").contents();
        assertThat(listContents).containsExactly(nonExistentTodo.toString());
        
        window.label("errorMessageLabel")
            .requireText("ToDo doesn't exist: Non-existent task");
    }
    
    @Test
    @GUITest
    void testAddAndDeleteFlow() {
        window.textBox("titleTextBox").enterText("Complete flow test");
        window.checkBox("doneCheckBox").check();
        window.button("addButton").click();
        
        String[] listContents = window.list("todoList").contents();
        assertThat(listContents).containsExactly(new ToDo("Complete flow test", true).toString());
        
        window.list("todoList").selectItem(0);
        window.button("deleteButton").click();
        
        assertThat(window.list("todoList").contents()).isEmpty();
    }
    
    @Test
    @GUITest
    void testFormResetAfterSuccessfulAdd() {
        window.textBox("titleTextBox").enterText("Test task");
        window.checkBox("doneCheckBox").check();
        window.button("addButton").click();
        
        window.textBox("titleTextBox").requireText("Test task");
        window.checkBox("doneCheckBox").requireSelected();
        
        window.label("errorMessageLabel").requireText(" ");
    }
}