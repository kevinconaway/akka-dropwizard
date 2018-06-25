package com.github.kevinconaway.akka.metrics.advice;

import akka.dispatch.MessageQueue;
import com.codahale.metrics.Timer;
import com.github.kevinconaway.akka.metrics.MonitorableEnvelope;
import net.bytebuddy.asm.Advice;

import java.util.concurrent.TimeUnit;

public class DequeueEnvelopeAdvice {

    /**
     * Called after {@link MessageQueue#dequeue()} returns
     */
    @Advice.OnMethodExit
    public static Object dequeue(
        @Advice.Return Object envelope,
        @Advice.FieldValue("waitTimer") Timer waitTimer
    ) {
        if (waitTimer != null && envelope != null) {
            MonitorableEnvelope monitorableEnvelope = (MonitorableEnvelope) envelope;

            Long enqueueTime = monitorableEnvelope.getEnqueueTime();

            long elapsed = System.nanoTime() - enqueueTime;

            waitTimer.update(elapsed, TimeUnit.NANOSECONDS);
        }

        return envelope;
    }
}
