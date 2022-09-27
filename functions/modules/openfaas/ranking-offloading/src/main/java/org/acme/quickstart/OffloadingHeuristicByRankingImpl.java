package org.acme.quickstart;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OffloadingHeuristicByRankingImpl implements OffloadingHeuristicByRanking {

    public boolean shouldOffloadVitalSigns(List<Integer> rankingsForAllServices, int calculatedRanking) throws CouldNotDetermineException {
        
        if (!rankingsForAllServices.isEmpty()) {
            List<Integer> normalizedRankings = rankingsForAllServices.stream()
                                                                     .sorted()
                                                                     .distinct()
                                                                     .collect(Collectors.toList());

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
