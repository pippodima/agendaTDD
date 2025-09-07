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
        try {
            getConnection().setAutoCommit(false);

            T result = code.apply(repository);

            getConnection().commit();

            return result;

        } catch (RuntimeException | SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Rollback failed", rollbackEx);
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            try {
                getConnection().setAutoCommit(true); 
            } catch (SQLException e) {
                throw new RuntimeException("Failed to reset auto-commit", e);
            }
        }
    }

	public Connection getConnection() {
		return connection;
	}
}
