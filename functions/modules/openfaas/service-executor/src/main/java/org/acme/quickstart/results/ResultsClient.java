package org.acme.quickstart.results;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "results")
@Path("/results/{id}")
@ApplicationScoped
public interface ResultsClient {

    @PATCH
    Void sendResults(@PathParam("id") String id, Results results);

}
