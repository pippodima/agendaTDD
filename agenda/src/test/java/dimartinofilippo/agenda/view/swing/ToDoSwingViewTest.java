package dimartinofilippo.agenda.view.swing;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToDoSwingViewTest {

    private FrameFixture window;
    private Robot robot;

    @BeforeEach
    void setUp() {
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(50); 
        
        ToDoSwingView todoSwingView = GuiActionRunner.execute(ToDoSwingView::new);

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

        window.textBox("titleTextBox").requireEnabled();
        window.textBox("doneTextBox").requireEnabled();
    }

}
