package com.github.kevinconaway.akka.metrics;

public class AkkaDropwizardSettings {

    public static final String DEFAULT_ROOT_PREFIX = "actor-metrics";

    private final ActorMetricPrefixStrategy metricPrefixStrategy;
    private final String rootPrefix;

    public AkkaDropwizardSettings(ActorMetricPrefixStrategy metricPrefixStrategy, String rootPrefix) {
        if (metricPrefixStrategy == null) {
            throw new IllegalArgumentException("Metric prefix strategy cannot be null");
        }

        if (rootPrefix == null || rootPrefix.isEmpty() || rootPrefix.equals("/")) {
            throw new IllegalArgumentException("Root metric prefix must be specified");
        }

        this.metricPrefixStrategy = metricPrefixStrategy;
        this.rootPrefix = rootPrefix.endsWith("/") ? rootPrefix.substring(0, rootPrefix.length() - 1) : rootPrefix;
    }

    public AkkaDropwizardSettings() {
        this(new DefaultActorMetricPrefixStrategy(), DEFAULT_ROOT_PREFIX);
    }

    public ActorMetricPrefixStrategy metricPrefixStrategy() {
        return metricPrefixStrategy;
    }

    public String rootPrefix() {
        return rootPrefix;
    }
}
