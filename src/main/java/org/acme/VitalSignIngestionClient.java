package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "vital-sign")
@Path("/vital-sign")
@ApplicationScoped
public interface VitalSignIngestionClient {

    @POST
    Void ingestVitalSigns(VigalSignIngestionClientInputDto input);
    
}
