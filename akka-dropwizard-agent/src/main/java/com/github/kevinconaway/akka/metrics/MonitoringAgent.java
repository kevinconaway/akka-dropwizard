package com.github.kevinconaway.akka.metrics;

import com.github.kevinconaway.akka.metrics.dynamic.AgentClassFileLocator;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.pool.TypePool;

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

        ClassFileLocator locator = AgentClassFileLocator.create();

        agentBuilder = instrumentEnvelope(agentBuilder);
        agentBuilder = instrumentMessageQueue(agentBuilder, locator);
        agentBuilder = instrumentMailbox(agentBuilder, locator);

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

    private static AgentBuilder instrumentMessageQueue(AgentBuilder agentBuilder, ClassFileLocator locator) {
        TypePool typePool = TypePool.Default.of(locator);
        TypeDescription messageQueue = typePool.describe("akka.dispatch.MessageQueue").resolve();
        TypeDescription timer = typePool.describe("com.codahale.metrics.Timer").resolve();
        TypeDescription monitorableMessageQueue = typePool.describe("com.github.kevinconaway.akka.metrics.MonitorableMessageQueue").resolve();

        return agentBuilder
            .type(
                isSubTypeOf(
                    messageQueue
                ).and(
                    not(isAbstract())
                )
            )
            .transform(
                new AgentBuilder.Transformer.ForAdvice()
                    .include(
                        locator
                    )
                    .advice(named("enqueue"), "com.github.kevinconaway.akka.metrics.advice.EnqueueEnvelopeAdvice")
                    .advice(named("dequeue"), "com.github.kevinconaway.akka.metrics.advice.DequeueEnvelopeAdvice")
                    .advice(named("cleanUp"), "com.github.kevinconaway.akka.metrics.advice.CleanupMessageQueueAdvice")
            )
            .transform(
                (builder, typeDescription, classLoader, module) ->
                    builder
                        .defineField("metricPrefix", String.class, Visibility.PRIVATE)
                        .defineField("waitTimer", timer, Visibility.PRIVATE)
                        .implement(monitorableMessageQueue).intercept(FieldAccessor.ofBeanProperty())
            );
    }

    private static AgentBuilder instrumentMailbox(AgentBuilder agentBuilder, ClassFileLocator locator) {
        TypePool typePool = TypePool.Default.of(locator);
        TypeDescription mailboxType = typePool.describe("akka.dispatch.MailboxType").resolve();

        return agentBuilder
            .type(
                isSubTypeOf(mailboxType)
            )
            .transform(
                new AgentBuilder.Transformer.ForAdvice()
                    .include(
                        locator
                    )
                    .advice(named("create"), "com.github.kevinconaway.akka.metrics.advice.CreateMessageQueueAdvice")
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
