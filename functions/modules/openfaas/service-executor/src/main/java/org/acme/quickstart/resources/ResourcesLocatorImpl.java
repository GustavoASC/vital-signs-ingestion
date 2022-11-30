package org.acme.quickstart.resources;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ResourcesLocatorImpl implements ResourcesLocator {

    private final MachineResourcesClient machineResourcesClient;

    public ResourcesLocatorImpl(@RestClient MachineResourcesClient machineResourcesClient) {
        this.machineResourcesClient = machineResourcesClient;
    }
    
    @Override
    public ResourcesLocatorResponse getUsedCpuPercentage() {
        var resources = machineResourcesClient.getMachineResources();
        return new ResourcesLocatorResponse(
            resources.getCpu(),
            resources.getLastObservation(),
            null,
            null
        );
    }

}
