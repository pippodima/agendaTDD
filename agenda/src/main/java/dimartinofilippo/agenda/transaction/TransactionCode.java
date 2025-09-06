package dimartinofilippo.agenda.transaction;

import java.util.function.Function;

import dimartinofilippo.agenda.repository.ToDoRepository;

@FunctionalInterface
public interface TransactionCode<T> extends Function<ToDoRepository, T> {

}
