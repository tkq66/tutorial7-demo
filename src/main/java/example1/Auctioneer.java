package example1;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

public class Auctioneer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Buyer.class);
	
	private StatefulRedisPubSubConnection<String, String> buyConnection;
	private StatefulRedisPubSubConnection<String, String> sellConnection;
	private RedisPubSubAsyncCommands<String, String> buyCommand;
	private RedisPubSubAsyncCommands<String, String> sellCommand;
	private int desiredPrice;
	private int allowedOffset; 
	
	public AuctioneerListener listener;
	public String id;
	public String status;
	public String productName;
	
	public Auctioneer(int desiredPrice, int allowedOffset, String id, String productName) {
		this.status = "NOT SOLD";
		this.desiredPrice = desiredPrice;
		this.allowedOffset = allowedOffset;
		this.id = id;
		this.productName = productName;
		this.listener = new AuctioneerListener();
		this.buyConnection = RedisSingleton.getRedisClient().connectPubSub();
		this.sellConnection = RedisSingleton.getRedisClient().connectPubSub();
		this.buyConnection .addListener(this.listener);
		this.buyCommand = this.buyConnection.async();
		this.sellCommand = this.sellConnection.async();
		this.buyCommand.subscribe(this.productName + "-offer");		
	}
	
	public RedisFuture<Long> sell (String buyerId) {
		return this.sellCommand.publish(this.productName + "-sell", buyerId);
	}
	
	public void disconnect() {
		RedisSingleton.destroy();
	}
	
	public class AuctioneerListener implements RedisPubSubListener<String, String> {
		
        private String message;

        public String getMessage() {
        	return message;
        }

	    public void message(String channel, String message) {
	    	if (!channel.equals(productName + "-offer")) {
	    		return;
	    	}
    		String[] messageContent = message.split("-");
    		if (status.equals("SOLD")) {
    			this.message = id + " - PRODUCT ALREADY SOLD - Buyer " + messageContent[0] + " offers " + messageContent[1] + " for " + productName;
    			LOGGER.info("{} - PRODUCT ALREADY SOLD - Buyer {} offers {} for {}", id, messageContent[0], messageContent[1], productName);
    			return;
    		}
    		this.message = id + " - Buyer " + messageContent[0] + " offers " + messageContent[1] + " for " + productName;
	    	LOGGER.info("{} - Buyer {} offers {} for {}", id, messageContent[0], messageContent[1], productName);
	    	int priceOffered = Integer.parseInt(messageContent[1]);
	    	if (priceOffered >= (desiredPrice - allowedOffset) &&
	    		priceOffered <= (desiredPrice + allowedOffset)) {
	    		this.message = id + " - SOLD - Buyer " + messageContent[0] + " offers " + messageContent[1] + " for " + productName;
	    		LOGGER.info("{} - SOLD - Buyer {} offers {} for {}", id, messageContent[0], messageContent[1], productName);
	    		status = "SOLD";
	    		RedisFuture<Long> sellResult = sell(messageContent[0]);
	    		try {
					sellResult.get(15, TimeUnit.SECONDS);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	} else {
	    		this.message = id + " - NOT SOLD - Buyer " + messageContent[0] + " offers " + messageContent[1] + " for " + productName;
	    		LOGGER.info("{} - NOT SOLD - Buyer {} offers {} for {}", id, messageContent[0], messageContent[1], productName);
	    	}
	    }

	    public void message(String pattern, String channel, String message) {

	    }

	    public void subscribed(String channel, long count) {
	    	LOGGER.debug("Subscribed to {}", channel);
	    }

	    public void psubscribed(String pattern, long count) {

	    }

	    public void unsubscribed(String channel, long count) {

	    }

	    public void punsubscribed(String pattern, long count) {

	    }
	}
}

