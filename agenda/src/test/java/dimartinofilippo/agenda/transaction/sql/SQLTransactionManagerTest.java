package dimartinofilippo.agenda.transaction.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dimartinofilippo.agenda.repository.ToDoRepository;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SQLTransactionManagerUnitTest {

	@Mock
	private Connection mockConnection;

	@Mock
	private ToDoRepository mockRepo;

	@InjectMocks
	private SQLTransactionManager txManager;

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
}
