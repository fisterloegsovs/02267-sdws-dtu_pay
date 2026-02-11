package startup;

import services.ReportingService;
import messaging.implementations.RabbitMqQueue;

public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        new ReportingService(new RabbitMqQueue("rabbitmq"));
    }
}
