package dimartinofilippo.agenda.transaction.mongo;

import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.transaction.TransactionCode;
import dimartinofilippo.agenda.transaction.TransactionManager;

public class MongoTransactionManager implements TransactionManager{
	
    private final ToDoRepository repository;


	public MongoTransactionManager(ToDoRepository repository) {
        this.repository = repository;
    }


	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		try {
            return code.apply(repository);
        } catch (RuntimeException e) {
            throw e;
        }	}

}
