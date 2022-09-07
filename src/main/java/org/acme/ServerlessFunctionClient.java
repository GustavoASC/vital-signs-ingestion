package org.acme;

import javax.ws.rs.POST;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface ServerlessFunctionClient {

    @POST
    void runAsyncHealthService(String payload);

}
