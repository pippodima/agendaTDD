package dimartinofilippo.agenda.model;

import java.util.Objects;

public class ToDo {
	private String title;
	private boolean done;

	public ToDo() {
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ToDo))
			return false;
		ToDo todo = (ToDo) o;
		return done == todo.done && Objects.equals(title, todo.title);
	}

	@Override
	public int hashCode() {
		return Objects.hash(title, done);
	}

	@Override
	public String toString() {
		return "ToDo{" + "title='" + title + '\'' + ", done=" + done + '}';
	}

}
