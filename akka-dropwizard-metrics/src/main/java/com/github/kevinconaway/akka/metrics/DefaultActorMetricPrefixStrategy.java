package com.github.kevinconaway.akka.metrics;

import akka.actor.ActorRef;

/**
 * Default strategy is to strip off the root actor path.  The prefix for
 * _akka://testing/user/echo-actor#-2124538033_ would be _user/echo-actor_
 */
public class DefaultActorMetricPrefixStrategy implements ActorMetricPrefixStrategy {

    @Override
    public String prefixFor(ActorRef actor) {
        String prefix = actor.path().toStringWithoutAddress().substring(1);
        prefix = prefix.replace("$", "");

        return prefix.length() == 0 ? "root" : prefix;
    }
}
