package com.formentor.magnolia.async;

import info.magnolia.module.ModuleLifecycleContext;
import lombok.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * This class is optional and represents the configuration for the magnolia-event-driven-service module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/magnolia-event-driven-service</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
@Data
public class AsyncService implements info.magnolia.module.ModuleLifecycle {
    private String mq_server;
    private String mq_queue;

    private KafkaProducer kafkaProducer;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        if (mq_server != null) {
            kafkaProducer = getKafkaProducer(mq_server);
        }
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }

    public KafkaProducer<String, String> getKafkaProducer(String server) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server); // localhost:9092
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(kafkaProps);
    }

}
