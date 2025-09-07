package dimartinofilippo.agenda.transaction;

public interface TransactionManager {
    <T> T doInTransaction(TransactionCode<T> code);

}
