package org.acme.quickstart;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.quickstart.mapping.MappingResolver;
import org.acme.quickstart.output.MappingResourceResponse;

@Path("/")
public class MappingResource {

    private final MappingResolver mappingResolver;

    public MappingResource(MappingResolver mappingResolver) {
        this.mappingResolver = mappingResolver;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MappingResourceResponse hello() {
        try {

           String destination = mappingResolver.resolveMappingForCurrentHostname();
           return new MappingResourceResponse(destination);

        } finally {

            System.out.println(("Invoking garbage collector..."));
            System.gc();
    
        }
    }
}