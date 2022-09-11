package org.acme;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/vital-sign")
public class VitalSignResource {

    private final VitalSignService vitalSignService;

    public VitalSignResource(VitalSignService vitalSignService) {
        this.vitalSignService = vitalSignService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(VitalSignInputDto inputDto) {
        vitalSignService.ingestVitalSignRunningAllServices(inputDto.getVitalSign(), inputDto.getUserPriority());
        return Response.accepted().build();
    }
}
