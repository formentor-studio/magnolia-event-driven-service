package com.formentor.magnolia.async.service;

public interface QueueProducerService {
    public void enqueueMessage(String queue, String key, String message) throws Exception;
}
