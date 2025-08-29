package dimartinofilippo.agenda.repository.mongo;

import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import dimartinofilippo.agenda.model.ToDo;
import dimartinofilippo.agenda.repository.ToDoRepository;

public class ToDoMongoRepository implements ToDoRepository{
	
	public static final String TODO_COLLECTION_NAME = "todos";
    public static final String AGENDA_DB_NAME = "agenda";

    private final MongoCollection<Document> todoCollection;
    
    public ToDoMongoRepository(MongoClient client) {
    	
    	todoCollection = client
    			.getDatabase(AGENDA_DB_NAME)
    			.getCollection(TODO_COLLECTION_NAME);
    }
    

	@Override
	public ToDo save(ToDo todo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ToDo> findByTitle(String title) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<ToDo> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteByTitle(String title) {
		// TODO Auto-generated method stub
		
	}

}
