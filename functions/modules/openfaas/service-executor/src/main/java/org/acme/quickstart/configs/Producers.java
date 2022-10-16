package org.acme.quickstart.configs;

import java.net.URI;
import java.time.Clock;

import javax.inject.Singleton;
import javax.ws.rs.Produces;

import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.acme.quickstart.serverless.ServiceExecutorClient;
import org.acme.quickstart.topology.TopologyMappingOutputDto;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Singleton
public class Producers {

    private static final String TOPOLOGY_FN = "topology-mapping";
    private final ServerlessFunctionClient serverlessFunctionClient;

    public Producers(@RestClient ServerlessFunctionClient serverlessFunctionClient) {
        this.serverlessFunctionClient = serverlessFunctionClient;
    }

    @Produces
    @Singleton
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Produces
    @Singleton
    public ServiceExecutorClient serviceExecutorClient() {

        TopologyMappingOutputDto topology = serverlessFunctionClient.getOffloadingDestination(TOPOLOGY_FN);

        return RestClientBuilder.newBuilder()
                    .baseUri(URI.create(topology.getMappedDestination()))
                    .build(ServiceExecutorClient.class);
    }

}
