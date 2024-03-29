package org.acme.quickstart.prediction;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "serverless-function")
@Path("/function/{fn_name}")
@ApplicationScoped
public interface ServerlessFunctionClient {

    @POST
    String runFunction(@PathParam("fn_name") String fnName, String payload);

}
