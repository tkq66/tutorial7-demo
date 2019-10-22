package example1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

public class Buyer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Buyer.class);
	
	private StatefulRedisPubSubConnection<String, String> buyConnection;
	private StatefulRedisPubSubConnection<String, String> sellConnection;
	private RedisPubSubAsyncCommands<String, String> buyCommand;
	private RedisPubSubAsyncCommands<String, String> sellCommand;
	
	public String id;
	public String status;
	public int money;
	public String watchedProduct;
	public BuyerListener listener;
	
	public Buyer(String id, int money) {
		this.status = "NOT RECEIVED";
		this.id = id;
		this.money = money;
		this.listener = new BuyerListener();
		this.buyConnection = RedisSingleton.getRedisClient().connectPubSub();
		this.sellConnection = RedisSingleton.getRedisClient().connectPubSub();
		this.sellConnection .addListener(this.listener);
		this.buyCommand = this.buyConnection.async();
		this.sellCommand = this.sellConnection.async();
	}
	
	public RedisFuture<Long> buy (String productChannel, String offering) {
		this.watchedProduct = productChannel;
		this.sellCommand.subscribe(this.watchedProduct + "-sell");
		return this.buyCommand.publish(productChannel + "-offer", this.id + "-" + offering);
	}
	
	public void disconnect() {
		RedisSingleton.destroy();
	}
	
	public class BuyerListener implements RedisPubSubListener<String, String> {
		
        private String message;

        public String getMessage() {
        	return message;
        }
		
	    public void message(String channel, String message) {
	    	if (!channel.equals(watchedProduct + "-sell")) {
	    		return;
	    	}
    		if (message.equals(id)) {
    			this.message = id + " - Received the product!";
    			LOGGER.info("{} - Received the product!", id);
		        status = "RECVEIVED";
    		} else {
    			this.message = id + " - Product " + watchedProduct + " sold to " + message; 
    			LOGGER.info("{} - Product {} sold to {}", id, watchedProduct, message);
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

