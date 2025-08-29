package dimartinofilippo.agenda.model;

public class ToDo {
	private String title;
	private boolean done;
	
	public ToDo() {}
	
	public ToDo(String title, boolean done) {
		this.title = title;
		this.done = done;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	
	

}
