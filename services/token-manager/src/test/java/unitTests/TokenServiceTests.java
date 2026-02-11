package unitTests;

import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import services.TokenService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class TokenServiceTests {


    private TokenService facade;
    private MessageQueue mockQueue;
    private Map<String, Consumer<Event>> topicHandlers;
    private ArgumentCaptor<Event> eventCaptor;


    // @Author s194309
    @BeforeEach
    void setUp() {
        mockQueue = mock(MessageQueue.class);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        topicHandlers = new HashMap<>();

        // Capture handlers during registration
        doAnswer(invocation -> {
            String topic = invocation.getArgument(0);
            Consumer<Event> handler = invocation.getArgument(1);
            topicHandlers.put(topic, handler);
            return null;
        }).when(mockQueue).addHandler(anyString(), any());

        facade = new TokenService(mockQueue);
    }


    /// @Author s194309
    @Test
    public void getToken_ReturnsToken() throws IOException {
       // Arrange

       RabbitMqQueue mockQueue = Mockito.mock(RabbitMqQueue.class);
       doNothing().when(mockQueue).publish(any(Event.class));
       var tokenFacade = new TokenService(mockQueue);

       //Act
       var result = tokenFacade.generateToken("customer1");
       var valid = tokenFacade.checkToken(result);

       //Assert
       Assertions.assertNotNull(result);
       Assertions.assertTrue(result instanceof UUID);
       Assertions.assertTrue(tokenFacade.tokens.size()==1);
       Assertions.assertTrue(valid);
    }

    /// @Author s233470
//    @Test
//    public void getXTokens_ReturnsTokens() throws IOException {
//        // Arrange
//        RabbitMqQueue mockQueue = Mockito.mock(RabbitMqQueue.class);
//        doNothing().when(mockQueue).publish(any(Event.class));
//        var tokenFacade = new TokenFacade(mockQueue);
//        var tokenRequested = 5;
//
//        //Act
//        var result = tokenFacade.generateXTokens("customer1", tokenRequested);
//
//        //Assert
//        Assertions.assertNotNull(result);
//        Assertions.assertTrue(result instanceof ArrayList<UUID>);
//        Assertions.assertTrue(tokenFacade.Tokens.size()==tokenRequested);
//        for (var token : result) {
//            Assertions.assertTrue(tokenFacade.checkToken(token));
//        }
//    }

    // @Author s194309
//    @Test
//    public void getXTokens_ThrowsException() throws IOException {
//        RabbitMqQueue mockQueue = Mockito.mock(RabbitMqQueue.class);
//        doNothing().when(mockQueue).publish(any(Event.class));
//        var tokenFacade = new TokenFacade(mockQueue);
//        var tokenRequested = 6;
//
//        //Act
//        Exception result = assertThrows(
//                IllegalArgumentException.class, // The expected exception type
//                () -> {
//                    // Code that should throw the exception
//                    tokenFacade.generateXTokens(tokenRequested,"customer1");
//                }
//        );
//
//        //Assert
//        Assertions.assertNotNull(result);
//        Assertions.assertTrue(result instanceof IllegalArgumentException);
//        }

    // @Author s233470
    @Test
    public void checkToken_ReturnValidToken(){
        // Arrange
        RabbitMqQueue mockQueue = Mockito.mock(RabbitMqQueue.class);
        doNothing().when(mockQueue).publish(any(Event.class));
        var tokenFacade = new TokenService(mockQueue);

        //Act
        var token = tokenFacade.generateToken("customer1");
        var result = tokenFacade.checkToken(token);

        //Assert
        Assertions.assertTrue(result);
        Assertions.assertFalse(tokenFacade.tokens.getFirst().getIsValid());
    }

    // @Author s194309
    @Test
    public void checkToken_ThrowsException(){
        // Arrange
        RabbitMqQueue mockQueue = Mockito.mock(RabbitMqQueue.class);
        doNothing().when(mockQueue).publish(any(Event.class));
        var tokenFacade = new TokenService(mockQueue);

        //Act
        var token = tokenFacade.generateToken("customer1");
        tokenFacade.checkToken(token);
        Exception result = assertThrows(
                IllegalArgumentException.class, // The expected exception type
                () -> {
                    // Code that should throw the exception
                    tokenFacade.checkToken(token);
                }
        );

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof IllegalArgumentException);
    }

    /// @Author s233470
    @Test
    public void removeToken_RemovesToken() throws IOException {
        // Arrange
        RabbitMqQueue mockQueue = Mockito.mock(RabbitMqQueue.class);
        doNothing().when(mockQueue).publish(any(Event.class));
        var tokenFacade = new TokenService(mockQueue);

        //Act
        var token = tokenFacade.generateToken("customer1");
        var result = tokenFacade.removeToken(token);

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertFalse(tokenFacade.tokens.getFirst().getIsValid());
    }


}



