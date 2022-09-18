package org.acme;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OffloadingHeuristicByRankingImpl implements OffloadingHeuristicByRanking {

    private final RankingCalculator rankingCalculator;
    private final RunningServicesProvider servicesProvider;

    public OffloadingHeuristicByRankingImpl(
                            RankingCalculator rankingCalculator,
                            RunningServicesProvider servicesProvider) {
        this.rankingCalculator = rankingCalculator;
        this.servicesProvider = servicesProvider;
    }

    public boolean shouldOffloadVitalSigns(int userPriority, String service) throws CouldNotDetermineException {

        int calculatedRanking = rankingCalculator.calculate(userPriority, service);
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
