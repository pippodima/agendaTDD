package dimartinofilippo.agenda.view.swing;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;

class ToDoSwingViewTest {

	private FrameFixture window;
	private Robot robot;
	ToDoSwingView todoSwingView;

	@Mock
	private AgendaController agendaController;

	private AutoCloseable closeableMocks;

	@Mock
	private JTextField txtTitle;

	@Mock
	private JButton btnAdd;

	@Mock
	private KeyEvent mockKeyEvent;

	@BeforeEach
	void setUp() {
		closeableMocks = MockitoAnnotations.openMocks(this);

		robot = BasicRobot.robotWithNewAwtHierarchy();
		robot.settings().delayBetweenEvents(50);

		todoSwingView = GuiActionRunner.execute(() -> {
			ToDoSwingView view = new ToDoSwingView();
			view.setAgendaController(agendaController);
			return view;
		});

		window = new FrameFixture(robot, todoSwingView);
		window.show();
	}

	@AfterEach
	void tearDown() throws Exception {
		if (window != null) {
			window.cleanUp();
		}
		closeableMocks.close();
	}

	@Test
	void testSetup() {
		window.requireVisible();
		window.requireTitle("Agenda - ToDo List");
	}

	@Test
	@GUITest
	void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Title")).requireVisible();
		window.label(JLabelMatcher.withText("Done")).requireVisible();

		window.textBox("titleTextBox").requireEnabled().requireText("");
		window.checkBox("doneCheckBox").requireEnabled().requireNotSelected();

		window.button("addButton").requireDisabled();
		window.button("deleteButton").requireDisabled();

		window.list("todoList").requireEnabled();
		window.list("todoList").requireItemCount(0);

		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	void testWhenTitleNonEmptyAddButtonShouldBeEnabled() {
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
	void testDeleteButtonShouldBeEnabledOnlyWhenAToDoIsSelected() {
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
	void testsShowAllToDosShouldAddToDoTitleToTheList() {
		ToDo todo1 = new ToDo("todo1", false);
		ToDo todo2 = new ToDo("todo2", true);

		GuiActionRunner.execute(() -> todoSwingView.showAllToDos(List.of(todo1, todo2)));

		String[] listContents = window.list("todoList").contents();

		assertThat(listContents).containsExactly(todo1.toString(), todo2.toString());
	}

	@Test
	void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(() -> todoSwingView.showError("Error"));

		window.label("errorMessageLabel").requireText("Error");
	}

	@Test
	void testToDoAddedShouldAddToDoToListAndResetError() {
		ToDo todo = new ToDo("todo1", true);
		GuiActionRunner.execute(() -> todoSwingView.addedToDo(todo));

		String[] listContents = window.list("todoList").contents();
		assertThat(listContents).containsExactly(todo.toString());

		window.label("errorMessageLabel").requireText(" ");

	}

	@Test
	void testToDoRemovedShouldRemoveTheToDoFromTheListAndResetTheErrorLabel() {
		ToDo todo1 = new ToDo("todo1", true);
		ToDo todo2 = new ToDo("todo2", false);

		GuiActionRunner.execute(() -> {
			DefaultListModel<ToDo> listModel = todoSwingView.getListTodosModel();
			listModel.addElement(todo1);
			listModel.addElement(todo2);
		});

		GuiActionRunner.execute(() -> todoSwingView.removedToDo(todo1));

		String[] listContents = window.list("todoList").contents();
		assertThat(listContents).containsExactly(todo2.toString());

		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	void testAddButtonShouldDelegateToControllerAddToDo() {
		window.textBox("titleTextBox").enterText("todo");
		window.button(JButtonMatcher.withText("Add ToDo")).click();

		verify(agendaController).addToDo(new ToDo("todo", false));
	}

	@Test
	void testDeleteButtonShouldDelegateToControllerDeleteToDo() {
		ToDo todo1 = new ToDo("todo1", true);
		ToDo todo2 = new ToDo("todo2", false);

		GuiActionRunner.execute(() -> {
			DefaultListModel<ToDo> listModel = todoSwingView.getListTodosModel();
			listModel.addElement(todo1);
			listModel.addElement(todo2);
		});

		window.list("todoList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete Selected")).click();

		verify(agendaController).deleteToDo(todo2);
	}

	@Test
	void main_shouldRunWithoutException() throws Exception {
		// call main in the EventQueue to mimic real launch
		EventQueue.invokeAndWait(() -> ToDoSwingView.main(new String[] {}));
	}

	@Test
	void testButtonEnableLogic_ShouldDisableWhenEmpty() {
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(!txtTitle.getText().trim().isEmpty());
			}
		};

		when(txtTitle.getText()).thenReturn("");
		btnAddEnabler.keyReleased(mockKeyEvent);

		verify(btnAdd).setEnabled(false);
	}

	@Test
	void testButtonEnableLogic_ShouldDisableWhenWhitespaceOnly() {
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(!txtTitle.getText().trim().isEmpty());
			}
		};

		when(txtTitle.getText()).thenReturn("   ");
		btnAddEnabler.keyReleased(mockKeyEvent);

		verify(btnAdd).setEnabled(false);
	}

	@Test
	void testButtonEnableLogic_ShouldEnableWhenNonEmpty() {
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(!txtTitle.getText().trim().isEmpty());
			}
		};

		when(txtTitle.getText()).thenReturn("Meeting");
		btnAddEnabler.keyReleased(mockKeyEvent);

		verify(btnAdd).setEnabled(true);
	}

	@Test
	void testIsTitleValid_ShouldReturnFalseForNull() {
		assertThat(todoSwingView.isTitleValid(null)).isFalse();
	}

	@Test
	void testIsTitleValid_ShouldReturnFalseForEmpty() {
		assertThat(todoSwingView.isTitleValid("")).isFalse();
	}

	@Test
	void testIsTitleValid_ShouldReturnFalseForWhitespace() {
		assertThat(todoSwingView.isTitleValid("   ")).isFalse();
	}

	@Test
	void testIsTitleValid_ShouldReturnTrueForNonEmpty() {
		assertThat(todoSwingView.isTitleValid("Meeting")).isTrue();
	}

}
