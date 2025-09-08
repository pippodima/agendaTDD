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
public class ToDoMongoRepositoryTest {

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
        when(mockClient.getDatabase(ToDoMongoRepository.AGENDA_DB_NAME))
            .thenReturn(mockDatabase);
        when(mockDatabase.getCollection(ToDoMongoRepository.TODO_COLLECTION_NAME))
            .thenReturn(mockCollection);
        
        todoRepository = new ToDoMongoRepository(mockClient);
    }

    @Test
    void testFindAllWhenDBIsEmpty() {
        // Arrange
        when(mockCollection.find()).thenReturn(mockFindIterable);
        when(mockFindIterable.spliterator()).thenReturn(Collections.<Document>emptyList().spliterator());
        
        // Act
        List<ToDo> result = todoRepository.findAll();
        
        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindAllWhenDBIsNotEmpty() {
        // Arrange
        Document doc1 = new Document("title", "todo1").append("done", true);
        Document doc2 = new Document("title", "todo2").append("done", false);
        List<Document> documents = Arrays.asList(doc1, doc2);
        
        when(mockCollection.find()).thenReturn(mockFindIterable);
        when(mockFindIterable.spliterator()).thenReturn(documents.spliterator());
        
        // Act
        List<ToDo> result = todoRepository.findAll();
        
        // Assert
        assertThat(result).containsExactly(
            new ToDo("todo1", true),
            new ToDo("todo2", false)
        );
    }

    @Test
    void testFindByTitleNotFound() {
        // Arrange
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Optional<ToDo> result = todoRepository.findByTitle("nonexistent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByTitleFound() {
        // Arrange
        Document doc = new Document("title", "task2").append("done", true);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(doc);
        
        // Act
        Optional<ToDo> result = todoRepository.findByTitle("task2");
        
        // Assert
        assertThat(result)
            .isPresent()
            .contains(new ToDo("task2", true));
    }

    @Test
    void testSave() {
        // Arrange
        ToDo todo = new ToDo("task1", false);
        Document expectedDocument = new Document("title", "task1").append("done", false);
        
        // Act
        ToDo result = todoRepository.save(todo);
        
        // Assert
        assertThat(result).isEqualTo(todo);
        verify(mockCollection).insertOne(expectedDocument);
    }

    @Test
    void testDelete() {
        // Act
        todoRepository.deleteByTitle("task1");
        
        // Assert
        verify(mockCollection).deleteOne(any(Bson.class));
    }
}