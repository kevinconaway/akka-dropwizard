package com.github.kevinconaway.akka.metrics;

import akka.actor.ActorRef;

public interface ActorMetricPrefixStrategy {

    /**
     * Determine the metric prefix to use for {@code actor}
     * @param actor ActorRef
     * @return The metric prefix
     */
    String prefixFor(ActorRef actor);
}
