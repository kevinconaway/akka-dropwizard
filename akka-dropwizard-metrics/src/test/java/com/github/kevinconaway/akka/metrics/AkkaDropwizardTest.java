package com.github.kevinconaway.akka.metrics;

import com.codahale.metrics.MetricRegistry;
import org.junit.After;
import org.junit.Test;

public class AkkaDropwizardTest {

    @After
    public void tearDown() {
        AkkaDropwizard.clear();
    }

    @Test(expected = IllegalStateException.class)
    public void testSettingsUnconfigured() {
        AkkaDropwizard.settings();
    }

    @Test(expected = IllegalStateException.class)
    public void testRegistryUnconfigured() {
        AkkaDropwizard.registry();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRegistry() {
        AkkaDropwizard.configure(null, new AkkaDropwizardSettings());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullSettings() {
        AkkaDropwizard.configure(new MetricRegistry(), null);
    }
}
