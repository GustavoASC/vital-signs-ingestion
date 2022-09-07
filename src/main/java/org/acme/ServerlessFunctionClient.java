package org.acme;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/async-function/{fn_name}")
public interface ServerlessFunctionClient {

    @POST
    Void runAsyncHealthService(@PathParam("fn_name") String fnName, String payload);

}
