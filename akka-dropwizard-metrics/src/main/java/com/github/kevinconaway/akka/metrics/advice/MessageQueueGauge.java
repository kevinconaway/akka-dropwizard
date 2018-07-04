package com.github.kevinconaway.akka.metrics.advice;

import akka.dispatch.MessageQueue;
import com.codahale.metrics.Gauge;

public class MessageQueueGauge implements Gauge<Integer> {
    private final MessageQueue queue;

    public MessageQueueGauge(MessageQueue queue) {
        this.queue = queue;
    }

    @Override
    public Integer getValue() {
        return queue.numberOfMessages();
    }
}
