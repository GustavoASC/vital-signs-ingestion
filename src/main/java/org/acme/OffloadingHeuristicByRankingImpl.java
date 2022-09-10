package org.acme;

import java.util.List;
import java.util.Map;

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
        Map<String, List<Integer>> rankingsForAllServices = servicesProvider.provideRankings();

        if (rankingsForAllServices.containsKey(service)) {
            List<Integer> rankingsForService = rankingsForAllServices.get(service)
                                                                     .stream()
                                                                     .sorted()
                                                                     .distinct()
                                                                     .toList();

            int intermediateRanking = intermediateRanking(rankingsForService);
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
