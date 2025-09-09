package dimartinofilippo.agenda.transaction.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.transaction.TransactionCode;

public class SQLTransactionManagerTest {

	@Mock
	Connection connection;
	@Mock
	ToDoRepository repository;
	SQLTransactionManager txManager;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		txManager = new SQLTransactionManager(repository, connection);
	}

	@Test
	void testCommitOnSuccess() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(true);

		String result = txManager.doInTransaction(repo -> "ok");

		verify(connection).setAutoCommit(false);
		verify(connection).commit();
		verify(connection).setAutoCommit(true);
		assertEquals("ok", result);
	}

	@Test
	void testRollbackOnException() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(true);

		assertThrows(RuntimeException.class, () -> txManager.doInTransaction(repo -> {
			throw new RuntimeException("fail");
		}));

		verify(connection).rollback();
		verify(connection).setAutoCommit(true);
	}
	
    @Test
    @SuppressWarnings("unchecked")
    void testDoInTransaction_runtimeExceptionRollsBack() throws SQLException {
        TransactionCode<Object> code = mock(TransactionCode.class);
        when(code.apply(repository)).thenThrow(new RuntimeException("Test exception"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> txManager.doInTransaction(code));

        assertEquals("Transaction failed", ex.getMessage());
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testDoInTransaction_rollbackFails() throws SQLException {
        TransactionCode<Object> code = mock(TransactionCode.class);
        when(code.apply(repository)).thenThrow(new RuntimeException("Test exception"));
        // make rollback fail
        doThrow(new SQLException("Rollback failed")).when(connection).rollback();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> txManager.doInTransaction(code));

        // rollback failed, so outer exception is "Rollback failed"
        assertEquals("Rollback failed", ex.getMessage());
        assertTrue(ex.getCause() instanceof SQLException);
        assertEquals("Rollback failed", ex.getCause().getMessage());

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testDoInTransaction_setAutoCommitFails() throws SQLException {
        // Arrange
        TransactionCode<Object> code = mock(TransactionCode.class);
        Object expectedResult = new Object();
        when(code.apply(repository)).thenReturn(expectedResult);

        // Force setAutoCommit(true) in finally to fail
        doNothing().when(connection).setAutoCommit(false); // initial call works
        doThrow(new SQLException("Auto-commit reset failed")).when(connection).setAutoCommit(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> txManager.doInTransaction(code));

        assertEquals("Failed to reset auto-commit", ex.getMessage());
        assertTrue(ex.getCause() instanceof SQLException);
        assertEquals("Auto-commit reset failed", ex.getCause().getMessage());

        verify(connection).setAutoCommit(false);
        verify(connection).setAutoCommit(true);
        verify(connection).commit(); // should still commit successfully
    }




}
