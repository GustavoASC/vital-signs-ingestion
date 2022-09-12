package org.acme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ServicePriorityLocatorTest {
    
    @Test
    public void shouldLocatePriorityForConfiguredService() {
        assertThat(new ServicePriorityLocatorImpl().locate("foo-function"))
            .isEqualTo(4);
    }
    
    @Test
    public void shouldAssumeDefaultPriorityForNonConfiguredService() {
        assertThat(new ServicePriorityLocatorImpl().locate("non-configured"))
            .isEqualTo(5);
    }
}
