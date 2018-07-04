package com.github.kevinconaway.akka.metrics.advice;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

public class StartsWithFilter implements MetricFilter {

    private final String prefix;

    public StartsWithFilter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean matches(String name, Metric metric) {
        return name.startsWith(prefix);
    }
}
