package com.github.kevinconaway.akka.metrics.advice;

import com.codahale.metrics.Gauge;

public class CompositeGauge implements Gauge<Integer> {

    private final Gauge<Integer> first;
    private final Gauge<Integer> second;

    public CompositeGauge(Gauge<Integer> first, Gauge<Integer> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Integer getValue() {
        return first.getValue() + second.getValue();
    }
}
