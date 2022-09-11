package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "serverless-function")
@Path("/async-function/{fn_name}")
@ApplicationScoped
public interface ServerlessFunctionClient {

    @POST
    Void runAsyncHealthService(@PathParam("fn_name") String fnName, String payload);

}
