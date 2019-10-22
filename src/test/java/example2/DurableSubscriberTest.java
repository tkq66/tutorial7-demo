package example2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jms.JMSException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import example2.DurableSubscriber;
import example2.Publisher;
import example2.Status;

public class DurableSubscriberTest {

  private static Publisher publisherDurableSubscriber;
  private static DurableSubscriber subscriber1DurableSubscriber,
  								   subscriber2DurableSubscriber;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    publisherDurableSubscriber = new Publisher();
    publisherDurableSubscriber.create("publisher-durablesubscriber", "durablesubscriber.t");

    subscriber1DurableSubscriber = new DurableSubscriber();
    subscriber1DurableSubscriber.create("subscriber1-durablesubscriber", "durablesubscriber.t", "durablesubscriber1");

    subscriber2DurableSubscriber = new DurableSubscriber();
    subscriber2DurableSubscriber.create("subscriber2-durablesubscriber", "durablesubscriber.t", "durablesubscriber2");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    publisherDurableSubscriber.closeConnection();
    subscriber1DurableSubscriber.removeDurableSubscriber();
    subscriber2DurableSubscriber.removeDurableSubscriber();
    subscriber1DurableSubscriber.closeConnection();
    subscriber2DurableSubscriber.closeConnection();
  }

  @Test
  public void Should_OnlyActiveSubscribersObserve_When_PublishOnce() {
    try {
      // nondurable subscriptions, will not receive messages sent while
      // the subscribers are not active
      subscriber2DurableSubscriber.closeConnection();

      publisherDurableSubscriber.notifyBuyer("Jiaxe", Status.RIPPED_OFF);

      // recreate a connection for the nondurable subscription
      subscriber2DurableSubscriber.create("subscriber2-durablesubscriber", "durablesubscriber.t", "durablesubscriber2");

      publisherDurableSubscriber.notifyBuyer("Jesse", Status.SUCCESS);

      String email1 = subscriber1DurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jiaxe. you got RIPPED OFF, SON.", email1);
      String email2 = subscriber2DurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jiaxe. you got RIPPED OFF, SON.", email2);
      
      String email3 = subscriber1DurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jesse. Your purchase had been processed successfully.", email3);
      String email4 = subscriber2DurableSubscriber.processPurchaseStatus(1000);
      assertEquals("Hello, Jesse. Your purchase had been processed successfully.", email4);
      
    } catch (JMSException e) {
      fail("a JMS Exception occurred");
    }
  }
}