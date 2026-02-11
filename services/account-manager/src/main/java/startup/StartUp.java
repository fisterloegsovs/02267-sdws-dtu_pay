package startup;

import messaging.implementations.RabbitMqQueue;
import model.AccountService;

public class StartUp {

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public static void main(String[] args) throws Exception {
		new StartUp().startUp();
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	private void startUp() throws Exception {
		new AccountService(new RabbitMqQueue("rabbitmq"));
	}
}
