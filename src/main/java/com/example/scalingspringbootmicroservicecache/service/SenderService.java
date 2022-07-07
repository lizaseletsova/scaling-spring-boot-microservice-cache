package com.example.scalingspringbootmicroservicecache.service;

import com.example.scalingspringbootmicroservicecache.model.Message;
import com.example.scalingspringbootmicroservicecache.configuration.RedisEventConfiguration;
import com.example.scalingspringbootmicroservicecache.exception.SendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The type Sender service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SenderService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CountDownLatch latch;

    /**
     * Send message to redis
     *
     * @param message Message
     */
    public void send(String message) {
        try {
            redisTemplate.convertAndSend(RedisEventConfiguration.TOPIC, new Message(message));
        } catch (QueryTimeoutException e) {
            log.error("RedisCommand timed out", e);
            throw new SendException("Queue not reachable");
        } catch (Exception e) {
            log.error("Unknown error while sending", e);
            throw new SendException("Something bad happened");
        }

        try {
            latch.await(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Error sending message", e);
        }
    }
}
