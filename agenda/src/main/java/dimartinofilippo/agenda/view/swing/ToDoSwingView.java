package dimartinofilippo.agenda.view.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.view.ToDoView;

public class ToDoSwingView extends JFrame implements ToDoView{
	
	private static final long serialVersionUID = 1;
	private JPanel contentPane;
	
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
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
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
