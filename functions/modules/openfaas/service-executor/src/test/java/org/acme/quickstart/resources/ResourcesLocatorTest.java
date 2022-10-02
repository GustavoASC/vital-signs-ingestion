package org.acme.quickstart.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResourcesLocatorTest {

    @Mock
    MachineResourcesClient machineResourcesClient;

    @InjectMocks
    ResourcesLocatorImpl resourcesLocator;

    @Test
    public void shouldRetrieveCpuWithoutBeingUpdated() {
        
        when(machineResourcesClient.getMachineResources())
            .thenReturn(new MachineResourcesOutputDto(14.6));

        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(14);
    }

}
