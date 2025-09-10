package dimartinofilippo.agenda.transaction.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.transaction.sql.SQLTransactionManager.TransactionException;
import dimartinofilippo.agenda.transaction.sql.SQLTransactionManager.TransactionRollbackException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SQLTransactionManagerTest {

	private TestAppender testAppender;

	@Mock
	private Connection mockConnection;

	@Mock
	private ToDoRepository mockRepo;

	@InjectMocks
	private SQLTransactionManager txManager;

	@BeforeEach
	void setupAppender() {
		testAppender = new TestAppender();
		testAppender.start();
		Logger logger = (Logger) LoggerFactory.getLogger(SQLTransactionManager.class);
		logger.setLevel(Level.WARN);
		logger.addAppender(testAppender);
	}

	@Test
	void testCommitIsCalledOnSuccess() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);

		txManager.doInTransaction(repo -> "ok");

		verify(mockConnection).setAutoCommit(false);
		verify(mockConnection).commit();
		verify(mockConnection).setAutoCommit(true);
	}

	@Test
	void testRollbackOnRuntimeException() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);

		assertThrows(SQLTransactionManager.TransactionException.class, () -> txManager.doInTransaction(repo -> {
			throw new RuntimeException("boom");
		}));

		verify(mockConnection).rollback();
		verify(mockConnection).setAutoCommit(true);
	}

	@Test
	void testRollbackOnSQLException() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);
		doThrow(new SQLException("db error")).when(mockConnection).commit();

		assertThrows(SQLTransactionManager.TransactionException.class,
				() -> txManager.doInTransaction(repo -> "whatever"));

		verify(mockConnection).rollback();
		verify(mockConnection).setAutoCommit(true);
	}

	@Test
	void testRuntimeExceptionWrappedInTransactionException() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);

		TransactionException ex = assertThrows(SQLTransactionManager.TransactionException.class,
				() -> txManager.doInTransaction(repo -> {
					throw new RuntimeException("boom"); // business logic fails
				}));

		assertEquals("Transaction failed due to runtime exception", ex.getMessage());
		assertTrue(ex.getCause() instanceof RuntimeException);

		verify(mockConnection).rollback();
		verify(mockConnection).setAutoCommit(true);
	}

	@Test
	void testSQLExceptionDuringCommit() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);
		doThrow(new SQLException("db error")).when(mockConnection).commit();

		TransactionException ex = assertThrows(SQLTransactionManager.TransactionException.class,
				() -> txManager.doInTransaction(repo -> "ok"));

		assertEquals("Transaction failed due to SQL exception", ex.getMessage());
		assertTrue(ex.getCause() instanceof SQLException);

		verify(mockConnection).rollback();
		verify(mockConnection).setAutoCommit(true);
	}

	@Test
	void testRollbackException() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);

		// Simulate failure: commit throws SQLException
		doThrow(new SQLException("commit failed")).when(mockConnection).commit();
		// Simulate rollback ALSO fails
		doThrow(new SQLException("rollback failed")).when(mockConnection).rollback();

		TransactionRollbackException ex = assertThrows(SQLTransactionManager.TransactionRollbackException.class,
				() -> txManager.doInTransaction(repo -> "whatever"));

		assertEquals("Transaction rollback failed", ex.getMessage());
		assertTrue(ex.getCause() instanceof SQLException);

		verify(mockConnection).rollback();
		verify(mockConnection).setAutoCommit(true);
	}

	@Test
	void testRestoreAutoCommitLogsWarningWhenSQLExceptionOccurs() throws Exception {
		when(mockConnection.isClosed()).thenReturn(false);
		doThrow(new SQLException("cannot restore")).when(mockConnection).setAutoCommit(true);

		txManager = new SQLTransactionManager(null, mockConnection);

		var method = SQLTransactionManager.class.getDeclaredMethod("restoreAutoCommit", boolean.class);
		method.setAccessible(true);
		method.invoke(txManager, true);

		boolean found = testAppender.events.stream().anyMatch(event -> event.getLevel() == Level.WARN
				&& event.getFormattedMessage().contains("Warning: Failed to restore auto-commit state: cannot restore"));

		assertTrue(found, "Expected warning log not found");
	}

	@Test
	void testRollbackNotCalledWhenConnectionClosed() throws Exception {
		when(mockConnection.isClosed()).thenReturn(true);

		txManager = new SQLTransactionManager(mockRepo, mockConnection);

		// Use reflection to call private rollbackTransaction()
		var method = SQLTransactionManager.class.getDeclaredMethod("rollbackTransaction");
		method.setAccessible(true);

		method.invoke(txManager);

		// rollback() should not be called because connection is closed
		verify(mockConnection, never()).rollback();
	}

	@Test
	void testRestoreAutoCommitNotCalledWhenConnectionClosed() throws Exception {
		when(mockConnection.isClosed()).thenReturn(true);

		txManager = new SQLTransactionManager(mockRepo, mockConnection);

		var method = SQLTransactionManager.class.getDeclaredMethod("restoreAutoCommit", boolean.class);
		method.setAccessible(true);

		method.invoke(txManager, true);

		// setAutoCommit() should not be called
		verify(mockConnection, never()).setAutoCommit(anyBoolean());
	}

	@Test
	void testReturnValueFromTransaction() throws SQLException {
		when(mockConnection.getAutoCommit()).thenReturn(true);

		String expected = "success";
		String actual = txManager.doInTransaction(repo -> expected);

		assertEquals(expected, actual);
		verify(mockConnection).commit();
	}

	private static class TestAppender extends AppenderBase<ILoggingEvent> {
		private final List<ILoggingEvent> events = new ArrayList<>();

		@Override
		protected void append(ILoggingEvent eventObject) {
			events.add(eventObject);
		}
	}

}
