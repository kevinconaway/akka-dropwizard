package com.github.kevinconaway.akka.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * Singleton class to capture the {@link com.codahale.metrics.MetricRegistry} and other configuration.
 *
 * This is a singleton because there isn't a great way to inject and retrieve arbitrary metadata in to an
 * {@link akka.actor.ActorSystem}
 */
public class AkkaDropwizard {

    private static volatile MetricRegistry registry;
    private static volatile AkkaDropwizardSettings settings;

    public static MetricRegistry registry() {
        if (registry == null) {
            throw new IllegalStateException("Registry has not been configured");
        }

        return registry;
    }

    public static AkkaDropwizardSettings settings() {
        if (settings == null) {
            throw new IllegalStateException("Settings have not been configured");
        }

        return settings;
    }

    public static void configure(MetricRegistry registry) {
        configure(registry, new AkkaDropwizardSettings());
    }

    public static void configure(MetricRegistry registry, AkkaDropwizardSettings settings) {
        if (registry == null) {
            throw new IllegalArgumentException("Registry cannot be null");
        }

        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null");
        }

        AkkaDropwizard.registry = registry;
        AkkaDropwizard.settings = settings;
    }

    public static void clear() {
        AkkaDropwizard.registry = null;
        AkkaDropwizard.settings = null;
    }
}
