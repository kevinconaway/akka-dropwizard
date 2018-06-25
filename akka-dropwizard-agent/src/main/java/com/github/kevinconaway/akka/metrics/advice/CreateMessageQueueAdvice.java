package com.github.kevinconaway.akka.metrics.advice;

import akka.actor.ActorRef;
import akka.dispatch.MessageQueue;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.github.kevinconaway.akka.metrics.AkkaDropwizard;
import com.github.kevinconaway.akka.metrics.AkkaDropwizardSettings;
import com.github.kevinconaway.akka.metrics.MonitorableMessageQueue;
import net.bytebuddy.asm.Advice;
import scala.Option;

import java.util.SortedMap;

public class CreateMessageQueueAdvice {

    @SuppressWarnings("unchecked")
    @Advice.OnMethodExit
    public static Object create(
        @Advice.Argument(0) Option<ActorRef> owner,
        @Advice.Return Object result
    ) {
        if (owner.isDefined()) {
            AkkaDropwizardSettings settings = AkkaDropwizard.settings();
            MetricRegistry metricRegistry = AkkaDropwizard.registry();

            String metricPrefix = settings.rootPrefix() + "/" + settings.metricPrefixStrategy().prefixFor(owner.get());

            MonitorableMessageQueue monitorableQueue = (MonitorableMessageQueue) result;
            monitorableQueue.setMetricPrefix(metricPrefix);
            monitorableQueue.setWaitTimer(
                metricRegistry.timer(metricPrefix + "/message-wait")
            );

            MessageQueue queue = (MessageQueue) result;

            String gaugeKey = metricPrefix + "/mailbox-size";

            SortedMap<String, Gauge> gauges = metricRegistry.getGauges();

            Gauge<Integer> existing = (Gauge<Integer>) gauges.get(gaugeKey);

            if (existing == null) {
                metricRegistry.register(metricPrefix + "/mailbox-size", new MessageQueueGauge(queue));
            } else {
                metricRegistry.remove(gaugeKey);
                metricRegistry.register(gaugeKey, new CompositeGauge(existing, new MessageQueueGauge(queue)));
            }

        }

        return result;
    }

    public static class CompositeGauge implements Gauge<Integer> {

        private final Gauge<Integer> first;
        private final Gauge<Integer> second;

        public CompositeGauge(Gauge<Integer> first, Gauge<Integer> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Integer getValue() {
            return first.getValue() + second.getValue();
        }
    }

    public static class MessageQueueGauge implements Gauge<Integer> {
        private final MessageQueue queue;

        public MessageQueueGauge(MessageQueue queue) {
            this.queue = queue;
        }

        @Override
        public Integer getValue() {
            return queue.numberOfMessages();
        }
    }
}
