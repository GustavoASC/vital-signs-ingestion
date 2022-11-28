package org.acme.quickstart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.quickstart.input.ServiceExecutorInputDto;

@Path("/")
public class ServiceExecutorResource {

    private final VitalSignService vitalSignService;

    public ServiceExecutorResource(VitalSignService vitalSignService) {
        this.vitalSignService = vitalSignService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(ServiceExecutorInputDto inputDto) {
        
        UUID id = Optional.ofNullable(inputDto.getId())
                           .orElseGet(UUID::randomUUID);

        String service = inputDto.getServiceName();
        if (service == null) {
            vitalSignService.ingestVitalSignRunningAllServices(id, inputDto.getVitalSign(), inputDto.getUserPriority());
        } else {
            vitalSignService.ingestVitalSign(id, List.of(service), inputDto.getVitalSign(), inputDto.getUserPriority());
        }
        return Response.ok().build();
    }

}