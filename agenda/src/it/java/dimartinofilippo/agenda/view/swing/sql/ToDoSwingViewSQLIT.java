package dimartinofilippo.agenda.view.swing.sql;


import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import dimartinofilippo.agenda.controller.AgendaController;
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