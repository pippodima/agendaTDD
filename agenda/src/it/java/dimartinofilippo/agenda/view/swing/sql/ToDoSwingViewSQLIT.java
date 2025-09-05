package dimartinofilippo.agenda.view.swing.sql;


import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.sql.ToDoSQLRepository;
import dimartinofilippo.agenda.view.swing.ToDoSwingView;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ToDoSwingViewSQLIT {
    
    private DataSource dataSource;
    private FrameFixture window;
    private ToDoSwingView todoSwingView;
    private AgendaController agendaController;
    private ToDoSQLRepository todoRepository;
    private Robot robot;
    
    @BeforeAll
    void setupDatabase() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:agenda_it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        this.dataSource = ds;
        
        createSchema();
    }
    
    @BeforeEach
    void setUp() {
        clearDatabase();
        
        todoRepository = new ToDoSQLRepository(dataSource);
        
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(50);
        
        todoSwingView = GuiActionRunner.execute(() -> {
            ToDoSwingView view = new ToDoSwingView();
            return view;
        });
        
        agendaController = new AgendaController(todoRepository, todoSwingView);
        todoSwingView.setAgendaController(agendaController);
        
        window = new FrameFixture(robot, todoSwingView);
        window.show();
    }
    
    @AfterEach
    void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
    }
    
    @Test
    @GUITest
    void testAllToDos() {
        ToDo todo1 = new ToDo("Buy groceries", false);
        ToDo todo2 = new ToDo("Complete project", true);
        todoRepository.save(todo1);
        todoRepository.save(todo2);
        
        GuiActionRunner.execute(() -> agendaController.allToDos());
        
        List<String> listContents = List.of(window.list("todoList").contents());
        assertThat(listContents).containsExactly(todo1.toString(), todo2.toString());
    }
    
    @Test
    @GUITest
    void testAddButtonSuccess() {
        window.textBox("titleTextBox").enterText("Learn Java SQL");
        window.checkBox("doneCheckBox").uncheck();
        window.button("addButton").click();
        
        String[] listContents = window.list("todoList").contents();
        assertThat(listContents).containsExactly(new ToDo("Learn Java SQL", false).toString());
    }
    
    @Test
    @GUITest
    void testAddButtonError() {
        todoRepository.save(new ToDo("Existing Task SQL", true));
        
        window.textBox("titleTextBox").enterText("Existing Task SQL");
        window.checkBox("doneCheckBox").uncheck();
        window.button("addButton").click();
        
        assertThat(window.list("todoList").contents()).isEmpty();
        
        window.label("errorMessageLabel")
            .requireText("same ToDo already in the agenda: Existing Task SQL");
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
        ToDo nonExistentTodo = new ToDo("Non-existent task SQL", false);
        GuiActionRunner.execute(() -> todoSwingView.getListTodosModel().addElement(nonExistentTodo));
        
        window.list("todoList").selectItem(0);
        window.button("deleteButton").click();
        
        String[] listContents = window.list("todoList").contents();
        assertThat(listContents).containsExactly(nonExistentTodo.toString());
        
        window.label("errorMessageLabel")
            .requireText("ToDo doesn't exist: Non-existent task SQL");
    }
    

    
    // helpers
  
    
    private void createSchema() {
        try (Connection c = dataSource.getConnection(); 
             Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS todos (" +
                           "title VARCHAR(255) PRIMARY KEY," +
                           "done BOOLEAN NOT NULL" +
                           ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create schema", e);
        }
    }
    
    private void clearDatabase() {
        try (Connection c = dataSource.getConnection(); 
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM todos");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }
    
    
}