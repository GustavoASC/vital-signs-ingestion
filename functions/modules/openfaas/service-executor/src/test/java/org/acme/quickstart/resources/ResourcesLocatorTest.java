package org.acme.quickstart.resources;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResourcesLocatorTest {

    @InjectMocks
    ResourcesLocatorImpl resourcesLocator;

    @Test
    public void shouldRetrieveCpuWithoutBeingUpdated() {
        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(0);
    }

    @Test
    public void shouldRetrieveCpuAfterBeingUpdated() {
        resourcesLocator.updateUsedCpuPercentage(37);
        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(37);
    }

}
