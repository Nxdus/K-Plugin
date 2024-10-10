package org.nxdus.core.shared.managers;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RedisManager {

    private final JedisPool jedisPool;
    private final Jedis connection;

    public RedisManager(ConfigManager configManager) {
        jedisPool = configManager.getConfigAsBoolean("redis.enable") && configManager.getConfigAsString("redis.username").isEmpty()
                ? new JedisPool(getPoolConfig(), configManager.getConfigAsString("redis.host"), configManager.getConfigAsInt("redis.port"), 0, configManager.getConfigAsString("redis.password"))
                : new JedisPool(getPoolConfig(), configManager.getConfigAsString("redis.host"), configManager.getConfigAsInt("redis.port"), 0, configManager.getConfigAsString("redis.username"), configManager.getConfigAsString("redis.password"));

        connection = jedisPool.getResource();
    }

    private JedisPoolConfig getPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(128);
        poolConfig.setMinIdle(6);
        poolConfig.setMaxWait(Duration.ofSeconds(5));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestOnCreate(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);

        return poolConfig;
    }

    public ConcurrentHashMap<String, JedisPubSub> pubSubList = new ConcurrentHashMap<>();

    public void unsubscribe(String channel) {
        if (pubSubList.containsKey(channel)) {
            pubSubList.get(channel).unsubscribe(channel);
            pubSubList.remove(channel);
        }
    }

    public void subscribe(String channel, OnMessageListener listener) {
        Runnable runnable = () -> {
            try (Jedis Subscriber = jedisPool.getResource()) {
                JedisPubSub PubSub = new JedisPubSub() {
                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        System.out.println("Subscribed: " + channel + " : " + subscribedChannels);
                    }

                    @Override
                    public void onMessage(String channel, String message) {
                        listener.onMessage(channel, message);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        System.out.println("Unsubscribed: " + channel + " : " + subscribedChannels);
                    }
                };

                pubSubList.put(channel, PubSub);
                Subscriber.subscribe(PubSub, channel);
            } catch (Exception e) {
                e.printStackTrace();

                try {
                    Thread.sleep(3000L);
                    System.out.println("try reconnect");
                    subscribe(channel, listener);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        new Thread(runnable).start();
    }

    public String getKey(String key) {
        return connection.get(key);
    }

    public String setKey(String key, String value) {
        return connection.set(key, value);
    }

    public String setKeyEx(String key, String value, long seconds) {
        return connection.setex(key, seconds, value);
    }

    public void delKey(String key, String value) {
        connection.del(key, value);
    }

    public void publish(String key, String value) {
        connection.publish(key, value);
    }

    public interface OnMessageListener {
        void onMessage(String channel, String message);
    }

}
