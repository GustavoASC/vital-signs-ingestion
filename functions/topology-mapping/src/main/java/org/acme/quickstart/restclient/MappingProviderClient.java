package org.acme.quickstart.restclient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "mapping-provider")
@Path("/nodes-mappings.properties")
@ApplicationScoped
public interface MappingProviderClient {

    @GET
    String getMappingProperties();

}
