package org.acme.quickstart.serverless;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.acme.quickstart.input.ServiceExecutorInputDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "service-executor")
@Path("/function/service-executor")
@ApplicationScoped
public interface ServiceExecutorClient {

    @POST
    Void runServiceExecutor(ServiceExecutorInputDto payload);

}
