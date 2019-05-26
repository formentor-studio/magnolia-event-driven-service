package com.formentor.magnolia.async.service.impl;

import com.formentor.magnolia.async.AsyncService;
import com.formentor.magnolia.async.service.QueueProducerService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;

public class KafkaQueueProducerServiceImpl implements QueueProducerService {
    private final KafkaProducer kafkaProducer;

    @Inject
    public KafkaQueueProducerServiceImpl(AsyncService definition) {
        this.kafkaProducer = definition.getKafkaProducer();
    }

    @Override
    public void enqueueMessage(String queue, String key, String message) throws Exception{
        ProducerRecord record = new ProducerRecord(queue, key, message);
        try {
            kafkaProducer.send(record).get();
        } catch (Exception e) {
            throw e;
        }
    }
}
