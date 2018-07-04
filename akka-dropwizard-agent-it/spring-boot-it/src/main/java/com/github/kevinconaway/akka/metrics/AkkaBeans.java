package com.github.kevinconaway.akka.metrics;

import akka.actor.ActorSystem;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaBeans {

    @Autowired
    @Bean(destroyMethod = "terminate")
    public ActorSystem actorSystem(MetricRegistry metrics) {
        AkkaDropwizard.configure(metrics);

        return ActorSystem.create("spring-boot");
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

}
