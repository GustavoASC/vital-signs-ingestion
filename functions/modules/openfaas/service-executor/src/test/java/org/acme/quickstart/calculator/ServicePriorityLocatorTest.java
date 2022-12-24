package org.acme.quickstart.calculator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ServicePriorityLocatorTest {
    
    @Test
    public void shouldLocatePriorityForConfiguredService() {
        assertThat(new ServicePriorityLocatorImpl().locate("body-temperature-monitor"))
            .isEqualTo(1);
    }
    
    @Test
    public void shouldAssumeDefaultPriorityForNonConfiguredService() {
        assertThat(new ServicePriorityLocatorImpl().locate("non-configured"))
            .isEqualTo(5);
    }
}
