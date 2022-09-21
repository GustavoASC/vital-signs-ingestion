package org.acme;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OffloadingHeuristicByRankingImpl implements OffloadingHeuristicByRanking {

    private final RunningServicesProvider servicesProvider;

    public OffloadingHeuristicByRankingImpl(RunningServicesProvider servicesProvider) {
        this.servicesProvider = servicesProvider;
    }

    public boolean shouldOffloadVitalSigns(int calculatedRanking) throws CouldNotDetermineException {

        List<Integer> rankingsForAllServices = servicesProvider.getRankingsForRunningServices();

        if (!rankingsForAllServices.isEmpty()) {
            List<Integer> normalizedRankings = rankingsForAllServices.stream()
                                                                     .sorted()
                                                                     .distinct()
                                                                     .toList();

            int intermediateRanking = intermediateRanking(normalizedRankings);
            if (calculatedRanking == intermediateRanking) {
                throw new CouldNotDetermineException();
            }

            return calculatedRanking < intermediateRanking;
        }
        return false;

    }

    private int intermediateRanking(List<Integer> rankingsForService) {
        int intermediateIndex = rankingsForService.size() / 2;
        return rankingsForService.get(intermediateIndex);
    }

}
