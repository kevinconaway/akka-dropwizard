package com.github.kevinconaway.akka.metrics;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AkkaDropwizardSettingsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullNamingStrategy() {
        new AkkaDropwizardSettings(null, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRootPrefix() {
        new AkkaDropwizardSettings(new DefaultActorMetricPrefixStrategy(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyRootPrefix() {
        new AkkaDropwizardSettings(new DefaultActorMetricPrefixStrategy(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlashRootPrefix() {
        new AkkaDropwizardSettings(new DefaultActorMetricPrefixStrategy(), "/");
    }

    @Test
    public void testSlashRemovedFromPrefix() {
        AkkaDropwizardSettings settings = new AkkaDropwizardSettings(new DefaultActorMetricPrefixStrategy(), "foo/");

        assertThat(settings.rootPrefix()).isEqualTo("foo");
    }
}
