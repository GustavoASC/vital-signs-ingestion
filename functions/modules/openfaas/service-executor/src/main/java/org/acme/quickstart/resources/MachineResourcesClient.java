package org.acme.quickstart.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "machine-resources")
@Path("/machine-resources")
@ApplicationScoped
public interface MachineResourcesClient {

    @GET
    MachineResourcesOutputDto getMachineResources();

}
