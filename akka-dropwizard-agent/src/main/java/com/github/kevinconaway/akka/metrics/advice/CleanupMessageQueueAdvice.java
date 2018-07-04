package com.github.kevinconaway.akka.metrics.advice;

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

}
