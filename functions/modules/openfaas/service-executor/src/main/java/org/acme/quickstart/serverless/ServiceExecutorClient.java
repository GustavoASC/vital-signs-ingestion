package org.acme.quickstart.serverless;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.acme.quickstart.input.ServiceExecutorInputDto;

@Path("/function/service-executor")
public interface ServiceExecutorClient {

    @POST
    byte[] runServiceExecutor(ServiceExecutorInputDto payload);

}
