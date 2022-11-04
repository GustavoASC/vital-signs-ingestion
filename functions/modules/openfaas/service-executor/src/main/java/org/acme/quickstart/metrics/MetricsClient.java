package org.acme.quickstart.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "metrics")
@Path("/metrics")
@ApplicationScoped
public interface MetricsClient {

    @POST
    Void sendMetrics(Metrics metrics);

}
