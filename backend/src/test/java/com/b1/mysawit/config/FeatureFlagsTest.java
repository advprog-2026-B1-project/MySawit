package com.b1.mysawit.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureFlagsTest {

    @Test
    void defaultDeliveryEnabled_isTrue() {
        FeatureFlags flags = new FeatureFlags();
        assertThat(flags.isDeliveryEnabled()).isTrue();
    }

    @Test
    void setDeliveryEnabled_false_updatesValue() {
        FeatureFlags flags = new FeatureFlags();
        flags.setDeliveryEnabled(false);
        assertThat(flags.isDeliveryEnabled()).isFalse();
    }

    @Test
    void setDeliveryEnabled_true_updatesValue() {
        FeatureFlags flags = new FeatureFlags();
        flags.setDeliveryEnabled(false);
        flags.setDeliveryEnabled(true);
        assertThat(flags.isDeliveryEnabled()).isTrue();
    }
}
