package dimartinofilippo.agenda.view.swing;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dimartinofilippo.agenda.model.ToDo;

class ToDoSwingViewTest {

    private FrameFixture window;
    private Robot robot;
    ToDoSwingView todoSwingView;

    @BeforeEach
    void setUp() {
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(50); 
        
        todoSwingView = GuiActionRunner.execute(ToDoSwingView::new);

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
    void testSetup() {
        window.requireVisible();
        window.requireTitle("Agenda - ToDo List");
    }
    
    @Test
    @GUITest
    void testControlsInitialStates() {
        window.label(JLabelMatcher.withText("Title"));
        window.label(JLabelMatcher.withText("Done"));

        window.textBox("titleTextBox").requireEnabled().requireText("");
        window.checkBox("doneCheckBox").requireEnabled().requireNotSelected();

        window.button("addButton").requireDisabled();
        window.button("deleteButton").requireDisabled();

        window.list("todoList").requireEnabled();
        window.list("todoList").requireItemCount(0);

        window.label("errorMessageLabel").requireText(" ");
    }
    
    @Test
    public void testWhenTitleNonEmptyAddButtonShouldBeEnabled() {
    	window.textBox("titleTextBox").enterText("todo1");
    	window.button(JButtonMatcher.withText("Add ToDo")).requireEnabled();
    }
    
    @Test
    void addButtonShouldRemainDisabledWhenTitleIsBlank() {

    	window.textBox("titleTextBox").setText("");
        window.button(JButtonMatcher.withText("Add ToDo")).requireDisabled();

        window.textBox("titleTextBox").setText("   ");
        window.button(JButtonMatcher.withText("Add ToDo")).requireDisabled();
    }
    
    @Test
    public void testDeleteButtonShouldBeEnabledOnlyWhenAToDoIsSelected() {
        ToDo todo = new ToDo("Buy milk", false);
        GuiActionRunner.execute(() -> todoSwingView.showAllToDos(List.of(todo)));

        JButtonFixture deleteButton = window.button("deleteButton");

        deleteButton.requireDisabled();

        window.list("todoList").selectItem(0);
        deleteButton.requireEnabled();

        window.list("todoList").clearSelection();
        deleteButton.requireDisabled();
    }
    
    @Test
    public void testsShowAllToDosShouldAddToDoTitleToTheList() {
        ToDo todo1 = new ToDo("todo1", false);
        ToDo todo2 = new ToDo("todo2", true);

        GuiActionRunner.execute(() -> todoSwingView.showAllToDos(List.of(todo1, todo2)));

        String[] listContents = window.list("todoList").contents();

        assertThat(listContents).containsExactly(todo1.toString(), todo2.toString());
    }
    
    @Test
    public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
        ToDo todo = new ToDo("Buy milk", false);

        GuiActionRunner.execute(() -> todoSwingView.showError("error message", todo));

        window.label("errorMessageLabel")
              .requireText("error message: " + todo);
    }






}
