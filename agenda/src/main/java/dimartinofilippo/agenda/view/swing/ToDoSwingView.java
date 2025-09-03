package dimartinofilippo.agenda.view.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.view.ToDoView;

public class ToDoSwingView extends JFrame implements ToDoView{
	
	private static final long serialVersionUID = 1;
	private JPanel contentPane;
    private JTextField txtTitle;
    private JTextField txtDone;


	
	// launch app
	public static void main (String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				ToDoSwingView frame = new ToDoSwingView();
				frame.setVisible(true);
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	//create frame
	
    public ToDoSwingView() {
        setTitle("Agenda - ToDo List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 500, 400);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{0, 0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0, 0};
        gbl_contentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        // Label for Title
        JLabel lblTitle = new JLabel("Title");
        GridBagConstraints gbc_lblTitle = new GridBagConstraints();
        gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
        gbc_lblTitle.anchor = GridBagConstraints.EAST;
        gbc_lblTitle.gridx = 0;
        gbc_lblTitle.gridy = 0;
        contentPane.add(lblTitle, gbc_lblTitle);

        // TextField for Title
        txtTitle = new JTextField();
        txtTitle.setName("titleTextBox"); // 👈 important for AssertJ Swing tests
        GridBagConstraints gbc_txtTitle = new GridBagConstraints();
        gbc_txtTitle.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtTitle.gridx = 1;
        gbc_txtTitle.gridy = 0;
        contentPane.add(txtTitle, gbc_txtTitle);
        txtTitle.setColumns(20);

        // Label for Done
        JLabel lblDone = new JLabel("Done");
        GridBagConstraints gbc_lblDone = new GridBagConstraints();
        gbc_lblDone.insets = new Insets(0, 0, 0, 5);
        gbc_lblDone.anchor = GridBagConstraints.EAST;
        gbc_lblDone.gridx = 0;
        gbc_lblDone.gridy = 1;
        contentPane.add(lblDone, gbc_lblDone);

        // TextField for Done
        txtDone = new JTextField();
        txtDone.setName("doneTextBox"); // 👈 important for tests
        GridBagConstraints gbc_txtDone = new GridBagConstraints();
        gbc_txtDone.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtDone.gridx = 1;
        gbc_txtDone.gridy = 1;
        contentPane.add(txtDone, gbc_txtDone);
        txtDone.setColumns(20);
    }
	@Override
	public void showAllToDos(List<ToDo> todos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addedToDo(ToDo todo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removedToDo(ToDo todo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(String string) {
		// TODO Auto-generated method stub
		
	}
	
	

}
