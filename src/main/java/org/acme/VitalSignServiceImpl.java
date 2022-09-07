package org.acme;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class VitalSignServiceImpl implements VitalSignService {
    private static final List<String> FUNCTIONS = List.of("foo-function", "bar-function");
    private static final int CRITICAL_CPU_USAGE = 95;

    private final ServerlessFunctionClient serverlessFunctionClient;
    private final VitalSignIngestionClient vitalSignIngestionClient;
    private final ResourcesLocator resourcesLocator;

    public VitalSignServiceImpl(
            @RestClient ServerlessFunctionClient serverlessFunctionClient,
            @RestClient VitalSignIngestionClient vitalSignIngestionClient,
            ResourcesLocator resourcesLocator) {
        this.serverlessFunctionClient = serverlessFunctionClient;
        this.vitalSignIngestionClient = vitalSignIngestionClient;
        this.resourcesLocator = resourcesLocator;
    }

    @Override
    public void ingestVitalSign(String vitalSign) {
        if (shouldOffloadToParent()) {
            vitalSignIngestionClient.ingestVitalSigns(vitalSign);
        } else {
            FUNCTIONS.stream()
                    .forEach(fn -> serverlessFunctionClient.runAsyncHealthService(fn, vitalSign));
        }
    }

    private boolean shouldOffloadToParent() {
        int usedCpu = resourcesLocator.usedCpuPercentage();
        return usedCpu >= CRITICAL_CPU_USAGE;
    }

}
