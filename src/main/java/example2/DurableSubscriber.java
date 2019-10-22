package example2;

import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurableSubscriber {

  private static final Logger LOGGER = LoggerFactory.getLogger(DurableSubscriber.class);

  private static final String NO_GREETING = "no greeting";

  private String clientId;
  private Connection connection;
  private Session session;
  private MessageConsumer messageConsumer;

  private String subscriptionName;

  public void create(String clientId, String topicName, String subscriptionName) throws JMSException {
    this.clientId = clientId;
    this.subscriptionName = subscriptionName;

    // create a Connection Factory, connection, session, and topic
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
    connectionFactory.setTrustAllPackages(true);
    connection = connectionFactory.createConnection();
    connection.setClientID(clientId);
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Topic topic = session.createTopic(topicName);

    // create a MessageConsumer for receiving messages
    messageConsumer = session.createDurableSubscriber(topic, subscriptionName);

    // start the connection in order to receive messages
    connection.start();
  }

  public void removeDurableSubscriber() throws JMSException {
    messageConsumer.close();
    session.unsubscribe(subscriptionName);
  }

  public void closeConnection() throws JMSException {
    connection.close();
  }

  public String processPurchaseStatus(int timeout) throws JMSException {

    String confirmationEmailMessage = "Invalid email";

    // read a message from the topic destination
    Message message = messageConsumer.receive(timeout);
    // check if a message was received
    if (message == null) {
    	LOGGER.debug(clientId + ": no message received");
    	LOGGER.info("EMAIL={}", confirmationEmailMessage);
        return confirmationEmailMessage;
    }
    // cast the message to the correct type
    ObjectMessage objectMessage = (ObjectMessage) message;
    // retrieve the message content
    Purchase purchase = (Purchase) objectMessage.getObject();
    LOGGER.debug(clientId + ": received message with text='{}'", purchase.toString());
    switch (purchase.purchaseStatus) {
	    case SUCCESS: 
			confirmationEmailMessage = "Hello, " + purchase.buyerId + ". Your purchase had been processed successfully.";
			break;
		case FAILED:
			confirmationEmailMessage = "Hello, " + purchase.buyerId + ". Your payment was declined. You will not be charged.";
			break;
		case RIPPED_OFF:
			confirmationEmailMessage = "Hello, " + purchase.buyerId + ". you got RIPPED OFF, SON.";
			break;
		default:
			break;
    }
    LOGGER.info("EMAIL={}", confirmationEmailMessage);
    return confirmationEmailMessage;
  }
}