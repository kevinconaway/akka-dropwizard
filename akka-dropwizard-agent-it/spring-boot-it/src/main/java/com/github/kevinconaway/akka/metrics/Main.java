package com.github.kevinconaway.akka.metrics;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ComponentScan
public class Main {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class);

        try {
            MetricRegistry registry = applicationContext.getBean(MetricRegistry.class);
            ActorSystem actorSystem = applicationContext.getBean(ActorSystem.class);

            ActorRef actor = actorSystem.actorOf(
                Props.create(EchoActor.class),
                "echo-actor"
            );

            String message = "test";

            CompletionStage<Object> stage = PatternsCS.ask(actor, message, TimeUnit.SECONDS.toMillis(1));

            assertThat(stage.toCompletableFuture().join()).isEqualTo(message);

            Timer timer = registry.timer(AkkaDropwizardSettings.DEFAULT_ROOT_PREFIX + "/user/echo-actor/message-wait");

            assertThat(timer.getCount()).isEqualTo(1);
        } finally {
            applicationContext.close();
        }
    }

}
