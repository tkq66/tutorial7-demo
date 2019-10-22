package example1;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.lettuce.core.RedisFuture;

public class RedisPubSubTest {
	
	  private static String product = "Macbook Pro 2018";
	  private static int price = 5500;
	  private static int offset = 200;
	  private static Auctioneer shadySeller;
	  private static Buyer Weicong,
	  			   		   David,
	  			   		   Lin;
	
	  @BeforeClass
	  public static void setUpBeforeClass() throws Exception {
		  shadySeller = new Auctioneer(price, offset, "xXXXx", product);
		  Weicong = new Buyer("Weicong", 10000);
		  David = new Buyer("David", 10000);
		  Lin = new Buyer("Lin", 10000);
	  }

	  @AfterClass
	  public static void tearDownAfterClass() throws Exception {
		  shadySeller.disconnect();
		  Weicong.disconnect();
		  David.disconnect();
		  Lin.disconnect();
	  }

	@Test
	public void testAuction() throws Exception {
		RedisFuture<Long> weicongResult = Weicong.buy(product, "1");
		weicongResult.get(15, TimeUnit.SECONDS);
		weicongResult.thenAccept(i -> assertTrue(shadySeller.listener.getMessage().equals("xXXXx - NOT SOLD - Buyer Weicong offers 1 for Macbook Pro 2018")));
		
		RedisFuture<Long> davidResult = David.buy(product, "5400");
		davidResult.get(15, TimeUnit.SECONDS);
		davidResult.thenAccept(i -> assertTrue(shadySeller.listener.getMessage().equals("xXXXx - SOLD - Buyer David offers 1 for Macbook Pro 2018")));
		
		RedisFuture<Long> linResult = Lin.buy(product, "5500");
		linResult.get(15, TimeUnit.SECONDS);
		linResult.thenAccept(i -> assertTrue(shadySeller.listener.getMessage().equals("xXXXx - PRODUCT ALREADY SOLD - Buyer David offers 1 for Macbook Pro 2018")));
	}

}
