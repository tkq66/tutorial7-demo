package example2;

import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

  private String clientId;
  private Connection connection;
  private Session session;
  private MessageProducer messageProducer;

  public void create(String clientId, String topicName) throws JMSException {
    this.clientId = clientId;

    // create a Connection Factory, connection, session, and topic
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
    connectionFactory.setTrustAllPackages(true);
    connection = connectionFactory.createConnection();
    connection.setClientID(clientId);
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Topic topic = session.createTopic(topicName);

    // create a MessageProducer for sending messages
    messageProducer = session.createProducer(topic);
  }

  public void closeConnection() throws JMSException {
    connection.close();
  }

  public void notifyBuyer(String buyerId, Status purchaseStatus) throws JMSException {
    // create a JMS ObjectMessage
    ObjectMessage msgMessage = session.createObjectMessage(new Purchase(buyerId, purchaseStatus));

    // send the message to the topic destination
    messageProducer.send(msgMessage);

    LOGGER.debug(clientId + ": sent message with buyerId='{}', status='{}'", buyerId, purchaseStatus);
  }
}