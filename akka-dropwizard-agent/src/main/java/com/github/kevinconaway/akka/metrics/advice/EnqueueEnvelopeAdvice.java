package com.github.kevinconaway.akka.metrics.advice;

import akka.actor.ActorRef;
import akka.dispatch.Envelope;
import com.github.kevinconaway.akka.metrics.MonitorableEnvelope;
import net.bytebuddy.asm.Advice;

public class EnqueueEnvelopeAdvice {

    /**
     * Called when entering {@link akka.dispatch.MessageQueue#enqueue(ActorRef, Envelope)}
     */
    @Advice.OnMethodEnter
    public static void enqueue(
        @Advice.Argument(1) Object envelope
    ) {
        MonitorableEnvelope monitorableEnvelope = (MonitorableEnvelope) envelope;
        monitorableEnvelope.setEnqueueTime(System.nanoTime());
    }
}
