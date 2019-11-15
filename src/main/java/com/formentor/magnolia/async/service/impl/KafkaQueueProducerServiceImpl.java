package com.formentor.magnolia.async.service.impl;

import com.formentor.magnolia.async.AsyncService;
import com.formentor.magnolia.async.service.QueueProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class KafkaQueueProducerServiceImpl implements QueueProducerService {
    private final AsyncService definition;
    private final Optional<KafkaProducer> kafkaProducer;

    @Inject
    public KafkaQueueProducerServiceImpl(AsyncService definition) {
        this.definition = definition;
        if (definition.isEnabled()) {
            kafkaProducer = Optional.ofNullable(getKafkaProducerInstance(definition.getMq_server()));
        } else {
            log.warn("magnolia-event-driven not started because it is disabled, check the value of config/enabled");
            kafkaProducer = Optional.empty();
        }
    }

    @Override
    public void enqueueMessage(final String queue, String key, String message) throws Exception {
        if (kafkaProducer.isPresent()) {
            /**
             * Topic name
             * [namespace].[queue]
             *
             * [queue] = mq_queue (module config) || queue (specific queue)
             */
            String topic = StringUtils.isBlank(definition.getMq_queue())? queue: definition.getMq_queue();
            final String namespace = definition.getMq_namespace();
            topic = StringUtils.isBlank(namespace)? topic: namespace + "." + topic;

            ProducerRecord record = new ProducerRecord(topic, key, message);
            try {
                log.warn("Sending message to kafka topic {}", topic);
                kafkaProducer.get().send(record).get();
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private KafkaProducer<String, String> getKafkaProducerInstance(String server) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server); // localhost:9092
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        log.warn("Starting Kafka producer {} {} {} ", kafkaProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG), kafkaProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG), kafkaProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        return new KafkaProducer<>(kafkaProps);
    }

}
