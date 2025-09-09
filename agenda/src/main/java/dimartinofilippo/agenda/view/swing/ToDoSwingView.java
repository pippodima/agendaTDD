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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		contentPane.add(lblTitle, gbc_lblTitle);

		txtTitle = new JTextField();
		txtTitle.setName("titleTextBox");
		GridBagConstraints gbc_txtTitle = new GridBagConstraints();
		gbc_txtTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTitle.insets = new Insets(0, 0, 5, 0);
		gbc_txtTitle.gridx = 1;
		gbc_txtTitle.gridy = 0;
		contentPane.add(txtTitle, gbc_txtTitle);
		txtTitle.setColumns(15);

		// Enable Add button only when title is non-empty
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(!txtTitle.getText().trim().isEmpty());
			}
		};
		txtTitle.addKeyListener(btnAddEnabler);

		// === Done ===
		JLabel lblDone = new JLabel("Done");
		GridBagConstraints gbc_lblDone = new GridBagConstraints();
		gbc_lblDone.anchor = GridBagConstraints.EAST;
		gbc_lblDone.insets = new Insets(0, 0, 5, 5);
		gbc_lblDone.gridx = 0;
		gbc_lblDone.gridy = 1;
		contentPane.add(lblDone, gbc_lblDone);

		chkDone = new JCheckBox();
		chkDone.setName("doneCheckBox");
		GridBagConstraints gbc_chkDone = new GridBagConstraints();
		gbc_chkDone.anchor = GridBagConstraints.WEST;
		gbc_chkDone.insets = new Insets(0, 0, 5, 0);
		gbc_chkDone.gridx = 1;
		gbc_chkDone.gridy = 1;
		contentPane.add(chkDone, gbc_chkDone);

		// === Add Button ===
		btnAdd = new JButton("Add ToDo");
		btnAdd.setName("addButton");
		btnAdd.setEnabled(false);
		btnAdd.addActionListener(e -> agendaController.addToDo(new ToDo(txtTitle.getText(), chkDone.isSelected())));
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.gridwidth = 2;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 2;
		contentPane.add(btnAdd, gbc_btnAdd);

		// === ToDo List ===
		listTodosModel = new DefaultListModel<>();
		listTodos = new JList<>(getListTodosModel());
		listTodos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listTodos.setName("todoList");

		listTodos.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				btnDeleteSelected.setEnabled(!listTodos.isSelectionEmpty());
			}
		});

		scrollPane = new JScrollPane(listTodos);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		contentPane.add(scrollPane, gbc_scrollPane);

		// === Delete Button ===
		btnDeleteSelected = new JButton("Delete Selected");
		btnDeleteSelected.setName("deleteButton");
		btnDeleteSelected.setEnabled(false);
		btnDeleteSelected.addActionListener(e -> agendaController.deleteToDo(listTodos.getSelectedValue()));

		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.gridwidth = 2;
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 4;
		contentPane.add(btnDeleteSelected, gbc_btnDelete);

		// === Error Message ===
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setName("errorMessageLabel");
		lblErrorMessage.setForeground(Color.RED);
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.gridwidth = 2;
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 5;
		contentPane.add(lblErrorMessage, gbc_lblError);
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

}
