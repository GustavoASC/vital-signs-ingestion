package org.acme.quickstart.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

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
    public void shouldRetrieveCpuWithoutLastObservation() {
        
        when(machineResourcesClient.getMachineResources())
            .thenReturn(new MachineResourcesOutputDto(new BigDecimal("14.6"), null, null, null));

        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(new ResourcesLocatorResponse(new BigDecimal("14.6"), null));
    }

    @Test
    public void shouldRetrieveCpuWithLastObservation() {
        
        when(machineResourcesClient.getMachineResources())
            .thenReturn(new MachineResourcesOutputDto(new BigDecimal("18.090625000000003"), new BigDecimal("19.3"), null, null));

        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(new ResourcesLocatorResponse(new BigDecimal("18.090625000000003"), new BigDecimal("19.3")));
    }

    @Test
    public void shouldRetrieveCpuWithLastObservationAndMemory() {
        
        when(machineResourcesClient.getMachineResources())
            .thenReturn(new MachineResourcesOutputDto(new BigDecimal("18.090625000000003"), new BigDecimal("19.3"), new BigDecimal("27.38"), null));

        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(new ResourcesLocatorResponse(new BigDecimal("18.090625000000003"), new BigDecimal("19.3"), new BigDecimal("27.38"), null));
    }

    @Test
    public void shouldRetrieveCpuWithLastObservationAndMemoryWithLastObservation() {
        
        when(machineResourcesClient.getMachineResources())
            .thenReturn(new MachineResourcesOutputDto(new BigDecimal("18.090625000000003"), new BigDecimal("19.3"), new BigDecimal("27.38"), new BigDecimal("29.54")));

        assertThat(resourcesLocator.getUsedCpuPercentage())
                .isEqualTo(new ResourcesLocatorResponse(new BigDecimal("18.090625000000003"), new BigDecimal("19.3"), new BigDecimal("27.38"), new BigDecimal("29.54")));
    }

}
