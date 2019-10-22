package example1;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

public class RedisSingleton {
	private static RedisClient redisClient;
	private static StatefulRedisConnection<String, String> redisConnection;
    
    
    private RedisSingleton(){}
    
    public static RedisClient getRedisClient () {
    	if (redisClient == null) {
    		redisClient = RedisClient.create("redis://localhost:6379");
    	}
    	return redisClient;
    }
    
    public static StatefulRedisConnection<String, String> getRedisConnection () {
    	RedisClient client = redisClient;
    	if (redisClient == null) {
    		redisClient = getRedisClient();
    	}
    	if (redisConnection == null) {
    		redisConnection = client.connect();
    	}
    	return redisConnection;
    }
    
    public static void destroy () {
    	if (redisClient == null || redisConnection == null) {
    		return;
    	}
    	redisConnection.close();
    }
}
