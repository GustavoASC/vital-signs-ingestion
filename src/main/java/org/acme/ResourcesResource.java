package org.acme;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/resources")
public class ResourcesResource {

    private final ResourceService resourceService;

    public ResourcesResource(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateResources(ResourcesInputDto inputDto) {
        resourceService.updateUsedCpuPercentage(inputDto.getCpu());
        return Response.ok().build();
    }
}
