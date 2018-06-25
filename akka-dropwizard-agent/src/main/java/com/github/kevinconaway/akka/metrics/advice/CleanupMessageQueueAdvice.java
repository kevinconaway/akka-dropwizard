package com.github.kevinconaway.akka.metrics.advice;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.github.kevinconaway.akka.metrics.AkkaDropwizard;
import net.bytebuddy.asm.Advice;

public class CleanupMessageQueueAdvice {

    @Advice.OnMethodExit
    public static void cleanUp(
        @Advice.FieldValue("metricPrefix") String metricPrefix
    ) {
        if (metricPrefix != null && !metricPrefix.isEmpty()) {
            MetricRegistry metricRegistry = AkkaDropwizard.registry();

            metricRegistry.removeMatching(new StartsWithFilter(metricPrefix));
        }
    }

    public static class StartsWithFilter implements MetricFilter {

        private final String prefix;

        public StartsWithFilter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean matches(String name, Metric metric) {
            return name.startsWith(prefix);
        }
    }

}
