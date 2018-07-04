package com.github.kevinconaway.akka.metrics;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This needs to be run with the agent in order to pass
 */
public class BasicAgentIT {

    @Test
    public void testAgent() {
        MetricRegistry registry = new MetricRegistry();
        AkkaDropwizard.configure(registry);

        ActorSystem actorSystem = ActorSystem.create("basic-test");

        ActorRef actor = actorSystem.actorOf(
            Props.create(EchoActor.class),
            "echo-actor"
        );

        String message = "test";

        CompletionStage<Object> stage = PatternsCS.ask(actor, message, TimeUnit.SECONDS.toMillis(1));

        assertThat(stage.toCompletableFuture().join()).isEqualTo(message);

        Timer timer = registry.timer(AkkaDropwizardSettings.DEFAULT_ROOT_PREFIX + "/user/echo-actor/message-wait");

        Assertions.assertThat(timer.getCount()).isEqualTo(1);

        actorSystem.terminate();
    }

}
