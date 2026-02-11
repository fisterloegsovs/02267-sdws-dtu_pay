package startup;

import messaging.implementations.RabbitMqQueue;
import services.TokenService;

public class StartUp {
	public static void main(String[] args) throws Exception {
		new StartUp().startUp();
	}

	private void startUp() throws Exception {
		new TokenService(new RabbitMqQueue("rabbitmq"));
	}
}
