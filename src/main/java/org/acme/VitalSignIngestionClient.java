package org.acme;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/vital-sign")
public interface VitalSignIngestionClient {

    @POST
    Void ingestVitalSigns(String vitalSigns);
    
}
