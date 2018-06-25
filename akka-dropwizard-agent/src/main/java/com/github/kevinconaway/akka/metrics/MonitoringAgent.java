package com.github.kevinconaway.akka.metrics;

import akka.dispatch.MailboxType;
import akka.dispatch.MessageQueue;
import com.codahale.metrics.Timer;
import com.github.kevinconaway.akka.metrics.advice.CleanupMessageQueueAdvice;
import com.github.kevinconaway.akka.metrics.advice.CreateMessageQueueAdvice;
import com.github.kevinconaway.akka.metrics.advice.DequeueEnvelopeAdvice;
import com.github.kevinconaway.akka.metrics.advice.EnqueueEnvelopeAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.FieldAccessor;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.isAbstract;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class MonitoringAgent {

    public static void premain(String argString, Instrumentation instrumentation) {
        Map<String, String> arguments = parseArguments(argString);
        AgentBuilder builder = builder();

        if ("true".equals(arguments.get("debug"))) {
            builder = withDebugListener(builder);
        }

        builder.installOn(instrumentation);
    }

    static AgentBuilder builder() {
        AgentBuilder agentBuilder = new AgentBuilder.Default();

        agentBuilder = instrumentEnvelope(agentBuilder);
        agentBuilder = instrumentMessageQueue(agentBuilder);
        agentBuilder = instrumentMailbox(agentBuilder);

        return agentBuilder;
    }

    private static AgentBuilder instrumentEnvelope(AgentBuilder agentBuilder) {
        return agentBuilder
            .type(
                named("akka.dispatch.Envelope")
            )
            .transform(
                (builder, typeDescription, classLoader, module) ->
                    builder
                        .defineField("_enqueuedTime", Long.class, Visibility.PRIVATE)
                        .implement(MonitorableEnvelope.class).intercept(FieldAccessor.ofField("_enqueuedTime"))

            );
    }

    private static AgentBuilder instrumentMessageQueue(AgentBuilder agentBuilder) {
        return agentBuilder
            .type(
                isSubTypeOf(
                    MessageQueue.class
                ).and(
                    not(isAbstract())
                )
            )
            .transform(
                new AgentBuilder.Transformer.ForAdvice()
                    .include(
                        EnqueueEnvelopeAdvice.class.getClassLoader(),
                        DequeueEnvelopeAdvice.class.getClassLoader(),
                        CleanupMessageQueueAdvice.class.getClassLoader()
                    )
                    .advice(named("enqueue"), EnqueueEnvelopeAdvice.class.getName())
                    .advice(named("dequeue"), DequeueEnvelopeAdvice.class.getName())
                    .advice(named("cleanUp"), CleanupMessageQueueAdvice.class.getName())
            )
            .transform(
                (builder, typeDescription, classLoader, module) ->
                    builder
                        .defineField("metricPrefix", String.class, Visibility.PRIVATE)
                        .defineField("waitTimer", Timer.class, Visibility.PRIVATE)
                        .implement(MonitorableMessageQueue.class).intercept(FieldAccessor.ofBeanProperty())
            );
    }

    private static AgentBuilder instrumentMailbox(AgentBuilder agentBuilder) {
        return agentBuilder
            .type(
                isSubTypeOf(MailboxType.class)
            )
            .transform(
                new AgentBuilder.Transformer.ForAdvice()
                    .include(
                        CreateMessageQueueAdvice.class.getClassLoader()
                    )
                    .advice(named("create"), CreateMessageQueueAdvice.class.getName())
            );
    }

    private static AgentBuilder withDebugListener(AgentBuilder agentBuilder) {
        return agentBuilder.with(Listener.StreamWriting.toSystemError());
    }

    static Map<String, String> parseArguments(String argString) {
        if (argString == null || argString.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> arguments = new HashMap<>();

        for (String token : argString.split(",")) {
            String trimmed = token.trim();
            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex == -1) {
                continue;
            }

            String key = trimmed.substring(0, equalsIndex);
            String value = trimmed.substring(equalsIndex + 1, trimmed.length());

            arguments.put(key, value);
        }

        return arguments;
    }
}
