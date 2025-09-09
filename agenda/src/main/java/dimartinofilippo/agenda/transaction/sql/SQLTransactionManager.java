package dimartinofilippo.agenda.transaction.sql;

import java.sql.Connection;
import java.sql.SQLException;

import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.transaction.TransactionCode;
import dimartinofilippo.agenda.transaction.TransactionManager;

public class SQLTransactionManager implements TransactionManager {

	private final ToDoRepository repository;
	private final Connection connection;

	public SQLTransactionManager(ToDoRepository repository, Connection connection) {
		this.repository = repository;
		this.connection = connection;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		boolean originalAutoCommit = true;
		try {
			originalAutoCommit = getConnection().getAutoCommit();
			getConnection().setAutoCommit(false);

			T result = code.apply(repository);

			getConnection().commit();
			return result;

		} catch (RuntimeException e) {
			rollbackTransaction();
			throw new TransactionException("Transaction failed due to runtime exception", e);
		} catch (SQLException e) {
			rollbackTransaction();
			throw new TransactionException("Transaction failed due to SQL exception", e);
		} finally {
			restoreAutoCommit(originalAutoCommit);
		}
	}

	private void rollbackTransaction() {
		try {
			if (!getConnection().isClosed()) {
				getConnection().rollback();
			}
		} catch (SQLException rollbackEx) {
			throw new TransactionRollbackException("Transaction rollback failed", rollbackEx);
		}
	}

	private void restoreAutoCommit(boolean originalAutoCommit) {
		try {
			if (!getConnection().isClosed()) {
				getConnection().setAutoCommit(originalAutoCommit);
			}
		} catch (SQLException e) {
			// Log the error but don't throw to avoid masking original exception
			System.err.println("Warning: Failed to restore auto-commit state: " + e.getMessage());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public static class TransactionException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public TransactionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class TransactionRollbackException extends RuntimeException {
		private static final long serialVersionUID = 2L;

		public TransactionRollbackException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}