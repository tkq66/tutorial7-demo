package example2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jms.JMSException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import example2.Publisher;
import example2.Status;
import example2.Subscriber;

public class SubscriberTest {

  private static Publisher publisherPublishSubscribe,
      					   publisherMultipleConsumers,
      					   publisherNonDurableSubscriber;
  private static Subscriber subscriberPublishSubscribe,
      						subscriber1MultipleConsumers,
      						subscriber2MultipleConsumers,
      						subscriber1NonDurableSubscriber,
      						subscriber2NonDurableSubscriber;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    publisherPublishSubscribe = new Publisher();
    publisherPublishSubscribe.create("publisher-publishsubscribe", "publishsubscribe.t");

    publisherMultipleConsumers = new Publisher();
    publisherMultipleConsumers.create("publisher-multipleconsumers", "multipleconsumers.t");

    publisherNonDurableSubscriber = new Publisher();
    publisherNonDurableSubscriber.create("publisher-nondurablesubscriber", "nondurablesubscriber.t");

    subscriberPublishSubscribe = new Subscriber();
    subscriberPublishSubscribe.create("subscriber-publishsubscribe", "publishsubscribe.t");

    subscriber1MultipleConsumers = new Subscriber();
    subscriber1MultipleConsumers.create("subscriber1-multipleconsumers", "multipleconsumers.t");

    subscriber2MultipleConsumers = new Subscriber();
    subscriber2MultipleConsumers.create("subscriber2-multipleconsumers", "multipleconsumers.t");

    subscriber1NonDurableSubscriber = new Subscriber();
    subscriber1NonDurableSubscriber.create("subscriber1-nondurablesubscriber", "nondurablesubscriber.t");

    subscriber2NonDurableSubscriber = new Subscriber();
    subscriber2NonDurableSubscriber.create("subscriber2-nondurablesubscriber", "nondurablesubscriber.t");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    publisherPublishSubscribe.closeConnection();
    publisherMultipleConsumers.closeConnection();
    publisherNonDurableSubscriber.closeConnection();

    subscriberPublishSubscribe.closeConnection();
    subscriber1MultipleConsumers.closeConnection();
    subscriber2MultipleConsumers.closeConnection();
    subscriber1NonDurableSubscriber.closeConnection();
    subscriber2NonDurableSubscriber.closeConnection();
  }

  @Test
  public void Should_ObserveMessageOnce_When_PublishOnce() {
    try {
      publisherPublishSubscribe.notifyBuyer("Jeanne", Status.SUCCESS);

      String email1 = subscriberPublishSubscribe.processPurchaseStatus(1000);
      assertEquals("Hello, Jeanne. Your purchase had been processed successfully.", email1);

      String email2 = subscriberPublishSubscribe.processPurchaseStatus(1000);
      assertEquals("Invalid email", email2);

    } catch (JMSException e) {
      fail("a JMS Exception occurred");
    }
  }

  @Test
  public void Should_AllSubscribersObserve_When_PublishOnce() {
    try {
      publisherMultipleConsumers.notifyBuyer("Joanne", Status.FAILED);

      String email1 = subscriber1MultipleConsumers.processPurchaseStatus(1000);
      assertEquals("Hello, Joanne. Your payment was declined. You will not be charged.", email1);

      String email2 = subscriber2MultipleConsumers.processPurchaseStatus(1000);
      assertEquals("Hello, Joanne. Your payment was declined. You will not be charged.", email2);

    } catch (JMSException e) {
      fail("a JMS Exception occurred");
    }
  }

  @Test
  public void Should_OnlyActiveSubscribersObserve_When_PublishOnce() {
    try {
      // nondurable subscriptions, will not receive messages sent while
      // the subscribers are not active
      subscriber2NonDurableSubscriber.closeConnection();

      publisherNonDurableSubscriber.notifyBuyer("Jiaxe", Status.RIPPED_OFF);

      // recreate a connection for the nondurable subscription
      subscriber2NonDurableSubscriber.create("subscriber2-nondurablesubscriber", "nondurablesubscriber.t");

      publisherNonDurableSubscriber.notifyBuyer("Jesse", Status.SUCCESS);

      String email1 = subscriber1NonDurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jiaxe. you got RIPPED OFF, SON.", email1);
      String email2 = subscriber1NonDurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jesse. Your purchase had been processed successfully.", email2);

      String email3 = subscriber2NonDurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jesse. Your purchase had been processed successfully.", email3);
      String email4 = subscriber2NonDurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Invalid email", email4);

    } catch (JMSException e) {
      fail("a JMS Exception occurred");
    }
  }
}