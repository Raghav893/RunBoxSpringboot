package com.raghav.runboxspringboot.worker.service;

import java.util.UUID;

public interface QueueService {

    void enqueue(UUID submissionId);
    UUID dequeue();
}
