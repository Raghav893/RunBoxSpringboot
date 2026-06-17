package com.raghav.runboxspringboot.worker.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RedisQueueService implements QueueService {
    private final RedisTemplate<String,String> redisTemplate;
    private static final String QUEUE_KEY = "execution-queue";

    public RedisQueueService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

    }

    @Override
    public void enqueue(UUID submissionId) {
        redisTemplate.opsForList().leftPush(QUEUE_KEY,submissionId.toString());
    }

    @Override
    public UUID dequeue() {
        String id = redisTemplate.opsForList().rightPop(QUEUE_KEY);
        return id!=null?UUID.fromString(id) :null;
    }
}
