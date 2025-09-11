package dimartinofilippo.agenda.view.swing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.view.ToDoView;

public class ToDoSwingView extends JFrame implements ToDoView {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField txtTitle;
	private JCheckBox chkDone;
	private JButton btnAdd;
	private JList<ToDo> listTodos;
	private DefaultListModel<ToDo> listTodosModel;
	private JScrollPane scrollPane;
	private JButton btnDeleteSelected;
	private JLabel lblErrorMessage;
	private transient AgendaController agendaController;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {

			ToDoSwingView frame = new ToDoSwingView();
			frame.setVisible(true);

		});
	}

	public ToDoSwingView() {
		setTitle("Agenda - ToDo List");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 400);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[] { 0, 0, 0 };
		gbl.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl);

		// === Title ===
		JLabel lblTitle = new JLabel("Title");
		GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.anchor = GridBagConstraints.EAST;
		gbcLblTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		contentPane.add(lblTitle, gbcLblTitle);

		txtTitle = new JTextField();
		txtTitle.setName("titleTextBox");
		GridBagConstraints gbcTxtTitle = new GridBagConstraints();
		gbcTxtTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcTxtTitle.insets = new Insets(0, 0, 5, 0);
		gbcTxtTitle.gridx = 1;
		gbcTxtTitle.gridy = 0;
		contentPane.add(txtTitle, gbcTxtTitle);
		txtTitle.setColumns(15);

		// Enable Add button only when title is non-empty
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(isTitleValid(txtTitle.getText()));
			}
		};
		txtTitle.addKeyListener(btnAddEnabler);

		// === Done ===
		JLabel lblDone = new JLabel("Done");
		GridBagConstraints gbcLblDone = new GridBagConstraints();
		gbcLblDone.anchor = GridBagConstraints.EAST;
		gbcLblDone.insets = new Insets(0, 0, 5, 5);
		gbcLblDone.gridx = 0;
		gbcLblDone.gridy = 1;
		contentPane.add(lblDone, gbcLblDone);

		chkDone = new JCheckBox();
		chkDone.setName("doneCheckBox");
		GridBagConstraints gbcChkDone = new GridBagConstraints();
		gbcChkDone.anchor = GridBagConstraints.WEST;
		gbcChkDone.insets = new Insets(0, 0, 5, 0);
		gbcChkDone.gridx = 1;
		gbcChkDone.gridy = 1;
		contentPane.add(chkDone, gbcChkDone);

		// === Add Button ===
		btnAdd = new JButton("Add ToDo");
		btnAdd.setName("addButton");
		btnAdd.setEnabled(false);
		btnAdd.addActionListener(e -> agendaController.addToDo(new ToDo(txtTitle.getText(), chkDone.isSelected())));
		GridBagConstraints gbcBtnAdd = new GridBagConstraints();
		gbcBtnAdd.gridwidth = 2;
		gbcBtnAdd.insets = new Insets(0, 0, 5, 0);
		gbcBtnAdd.gridx = 0;
		gbcBtnAdd.gridy = 2;
		contentPane.add(btnAdd, gbcBtnAdd);

		// === ToDo List ===
		listTodosModel = new DefaultListModel<>();
		listTodos = new JList<>(getListTodosModel());
		listTodos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listTodos.setName("todoList");

		listTodos.addListSelectionListener(e -> btnDeleteSelected.setEnabled(!listTodos.isSelectionEmpty()));

		scrollPane = new JScrollPane(listTodos);
		GridBagConstraints gbcScrollPane = new GridBagConstraints();
		gbcScrollPane.gridwidth = 2;
		gbcScrollPane.insets = new Insets(0, 0, 5, 0);
		gbcScrollPane.fill = GridBagConstraints.BOTH;
		gbcScrollPane.gridx = 0;
		gbcScrollPane.gridy = 3;
		contentPane.add(scrollPane, gbcScrollPane);

		// === Delete Button ===
		btnDeleteSelected = new JButton("Delete Selected");
		btnDeleteSelected.setName("deleteButton");
		btnDeleteSelected.setEnabled(false);
		btnDeleteSelected.addActionListener(e -> agendaController.deleteToDo(listTodos.getSelectedValue()));

		GridBagConstraints gbcBtnDelete = new GridBagConstraints();
		gbcBtnDelete.gridwidth = 2;
		gbcBtnDelete.insets = new Insets(0, 0, 5, 0);
		gbcBtnDelete.gridx = 0;
		gbcBtnDelete.gridy = 4;
		contentPane.add(btnDeleteSelected, gbcBtnDelete);

		// === Error Message ===
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setName("errorMessageLabel");
		lblErrorMessage.setForeground(Color.RED);
		GridBagConstraints gbcLblError = new GridBagConstraints();
		gbcLblError.gridwidth = 2;
		gbcLblError.gridx = 0;
		gbcLblError.gridy = 5;
		contentPane.add(lblErrorMessage, gbcLblError);
	}

	@Override
	public void showAllToDos(List<ToDo> todos) {
		getListTodosModel().clear();
		todos.forEach(getListTodosModel()::addElement);
	}

	@Override
	public void addedToDo(ToDo todo) {
		listTodosModel.addElement(todo);
		resetErrorLabel();
	}

	@Override
	public void removedToDo(ToDo todo) {
		listTodosModel.removeElement(todo);
		resetErrorLabel();
	}

	@Override
	public void showError(String string) {
		lblErrorMessage.setText(string);
	}

	public DefaultListModel<ToDo> getListTodosModel() {
		return listTodosModel;
	}

	private void resetErrorLabel() {
		lblErrorMessage.setText(" ");
	}

	public void setAgendaController(AgendaController agendaController) {
		this.agendaController = agendaController;
	}

	// helper

	boolean isTitleValid(String text) {
		return text != null && !text.trim().isEmpty();
	}

}
