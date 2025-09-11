package dimartinofilippo.agenda.transaction.mongo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.transaction.TransactionCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MongoTransactionManagerTest {

	@Mock
	private ToDoRepository repository;

	private MongoTransactionManager txManager;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		txManager = new MongoTransactionManager(repository);
	}

	@Test
	void doInTransaction_shouldReturnResultFromCode() {
		TransactionCode<String> code = repo -> "success";

		String result = txManager.doInTransaction(code);

		assertThat(result).isEqualTo("success");
	}

	@Test
	void doInTransaction_shouldPropagateRuntimeException() {
		TransactionCode<Void> code = repo -> {
			throw new RuntimeException("fail");
		};

		RuntimeException ex = assertThrows(RuntimeException.class, () -> txManager.doInTransaction(code));
		assertThat(ex.getMessage()).isEqualTo("fail");
	}
}
