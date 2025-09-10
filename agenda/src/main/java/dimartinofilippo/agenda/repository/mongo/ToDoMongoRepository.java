package dimartinofilippo.agenda.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;

public class ToDoMongoRepository implements ToDoRepository{
	
	public static final String TODO_COLLECTION_NAME = "todos";
	public static final String AGENDA_DB_NAME = "agenda";
    public static final String TITLE = "title";

    private final MongoCollection<Document> todoCollection;
    
    public ToDoMongoRepository(MongoClient client) {
    	
    	todoCollection = client
    			.getDatabase(AGENDA_DB_NAME)
    			.getCollection(TODO_COLLECTION_NAME);
    }
    

	@Override
	public ToDo save(ToDo todo) {
	    Document doc = new Document()
	            .append(TITLE, todo.getTitle())
	            .append("done", todo.isDone());
	        todoCollection.insertOne(doc);
	        return todo;
	}

	@Override
	public Optional<ToDo> findByTitle(String title) {
		Document doc = todoCollection.find(Filters.eq(TITLE, title)).first();
		if (doc != null) {
			return Optional.of(fromDocumentToToDo(doc));
		}
		return Optional.empty();
	}

	private ToDo fromDocumentToToDo(Document doc) {
	    return new ToDo(
	            doc.getString(TITLE),
	            doc.getBoolean("done", false)
	        );
	}


	@Override
	public List<ToDo> findAll() {
	    return StreamSupport.stream(todoCollection.find().spliterator(), false)
	                        .map(this::fromDocumentToToDo)
	                        .toList();
	}

	@Override
	public void deleteByTitle(String title) {
		todoCollection.deleteOne(Filters.eq(TITLE, title));
		
	}

}
