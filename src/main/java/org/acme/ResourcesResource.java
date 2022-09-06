package org.acme;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/resources")
public class ResourcesResource {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        return Response.ok().build();
    }
}
