package admin;

import jakarta.inject.Singleton;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

	/**
	 * @author Asher Sharif (240193)
	 */
@Singleton
public class AdminFacade {

	private final MessageQueue queue;

	public AdminFacade() {
		this(new RabbitMqQueue("rabbitmq"));
	}

	public AdminFacade(MessageQueue queue) {
		this.queue = queue;
	}

	public List<Object> getAllTransactions() throws InterruptedException, ExecutionException {
		CompletableFuture<List<Object>> transactionsFuture = new CompletableFuture<>();

		queue.addHandler("AllTransactions", e -> {
			transactionsFuture.complete(e.getArgument(0, List.class));
		});

		queue.publish(new Event("GetAllTransactions", null));


		return transactionsFuture.get();
	}
}

