package org.acme.quickstart.resources;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ResourcesLocatorImpl implements ResourcesLocator {

    private final MachineResourcesClient machineResourcesClient;

    public ResourcesLocatorImpl(@RestClient MachineResourcesClient machineResourcesClient) {
        this.machineResourcesClient = machineResourcesClient;
    }
    
    @Override
    public BigDecimal getUsedCpuPercentage() {
        return machineResourcesClient.getMachineResources().getCpu();
    }

}
