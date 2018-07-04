package com.github.kevinconaway.akka.metrics;

import akka.actor.AbstractActor;

public class EchoActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchAny(m -> getSender().tell(m, getSelf()))
            .build();
    }

}
