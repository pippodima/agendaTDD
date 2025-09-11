package dimartinofilippo.agenda.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dimartinofilippo.agenda.model.ToDo;

@ExtendWith(MockitoExtension.class)
class ToDoMongoRepositoryTest {

	@Mock
	private MongoClient mockClient;

	@Mock
	private MongoDatabase mockDatabase;

	@Mock
	private MongoCollection<Document> mockCollection;

	@Mock
	private FindIterable<Document> mockFindIterable;

	private ToDoMongoRepository todoRepository;

	@BeforeEach
	void setup() {
		when(mockClient.getDatabase(ToDoMongoRepository.AGENDA_DB_NAME)).thenReturn(mockDatabase);
		when(mockDatabase.getCollection(ToDoMongoRepository.TODO_COLLECTION_NAME)).thenReturn(mockCollection);

		todoRepository = new ToDoMongoRepository(mockClient);
	}

	@Test
	void testFindAllWhenDBIsEmpty() {
		when(mockCollection.find()).thenReturn(mockFindIterable);
		when(mockFindIterable.spliterator()).thenReturn(Collections.<Document>emptyList().spliterator());

		List<ToDo> result = todoRepository.findAll();

		assertThat(result).isEmpty();
	}

	@Test
	void testFindAllWhenDBIsNotEmpty() {
		Document doc1 = new Document("title", "todo1").append("done", true);
		Document doc2 = new Document("title", "todo2").append("done", false);
		List<Document> documents = Arrays.asList(doc1, doc2);

		when(mockCollection.find()).thenReturn(mockFindIterable);
		when(mockFindIterable.spliterator()).thenReturn(documents.spliterator());

		List<ToDo> result = todoRepository.findAll();

		assertThat(result).containsExactly(new ToDo("todo1", true), new ToDo("todo2", false));
	}

	@Test
	void testFindByTitleNotFound() {
		when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
		when(mockFindIterable.first()).thenReturn(null);

		Optional<ToDo> result = todoRepository.findByTitle("nonexistent");

		assertThat(result).isEmpty();
	}

	@Test
	void testFindByTitleFound() {
		Document doc = new Document("title", "task2").append("done", true);

		when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
		when(mockFindIterable.first()).thenReturn(doc);

		Optional<ToDo> result = todoRepository.findByTitle("task2");

		assertThat(result).isPresent().contains(new ToDo("task2", true));
	}

	@Test
	void testSave() {
		ToDo todo = new ToDo("task1", false);
		Document expectedDocument = new Document("title", "task1").append("done", false);

		ToDo result = todoRepository.save(todo);

		assertThat(result).isEqualTo(todo);
		verify(mockCollection).insertOne(expectedDocument);
	}

	@Test
	void testDelete() {
		todoRepository.deleteByTitle("task1");

		verify(mockCollection).deleteOne(any(Bson.class));
	}
}