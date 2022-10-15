package org.acme.quickstart.restclient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "mapping-provider")
@Path("/nodes-mappings.properties")
@ApplicationScoped
public interface MappingProviderClient {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getMappingProperties();

}
