package com.formentor.magnolia.async;

import info.magnolia.module.ModuleLifecycleContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is optional and represents the configuration for the magnolia-event-driven-service module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/magnolia-event-driven-service</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
@Data
@Slf4j
public class AsyncService implements info.magnolia.module.ModuleLifecycle {
    private String mq_server;
    private String mq_queue;
    private boolean enabled;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }
}
