package org.acme.quickstart.offloading.ranking;

import javax.enterprise.context.ApplicationScoped;

import org.acme.quickstart.RunningServicesProvider;
import org.acme.quickstart.offloading.shared.CouldNotDetermineException;
import org.acme.quickstart.offloading.shared.OffloadingDecision;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OffloadingHeuristicByRankingImpl implements OffloadingHeuristicByRanking {

    private static final String FUNCTION_NAME = "ranking-offloading";

    private final RunningServicesProvider servicesProvider;
    private final ServerlessFunctionClient serverlessFunctionClient;

    public OffloadingHeuristicByRankingImpl(
            RunningServicesProvider servicesProvider,
            @RestClient ServerlessFunctionClient serverlessFunctionClient) {
        this.servicesProvider = servicesProvider;
        this.serverlessFunctionClient = serverlessFunctionClient;
    }

    public boolean shouldOffloadVitalSigns(int calculatedRanking) throws CouldNotDetermineException {

        OffloadingDecision decision = serverlessFunctionClient.runRankingOffloading(
            FUNCTION_NAME, createFunctionInput(calculatedRanking)
        ).getOffloadingDecision();

        if (decision == OffloadingDecision.UNKNOWN) {
            throw new CouldNotDetermineException();
        }
        return decision == OffloadingDecision.OFFLOAD;

    }

    private OffloadRankingInputDto createFunctionInput(int calculatedRanking) {
        return new OffloadRankingInputDto(
                servicesProvider.getRankingsForRunningServices(),
                calculatedRanking
        );
    }

}
