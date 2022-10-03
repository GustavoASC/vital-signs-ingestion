package org.acme.quickstart.offloading.ranking;

import java.util.List;
import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OffloadRankingInputDto {

    private final List<Integer> allRankings;
    private final int calculatedRanking;

    public OffloadRankingInputDto(List<Integer> allRankings, int calculatedRanking) {
        this.allRankings = allRankings;
        this.calculatedRanking = calculatedRanking;
    }

    public List<Integer> getAllRankings() {
        return this.allRankings;
    }

    public int getCalculatedRanking() {
        return this.calculatedRanking;
    }

    @Override
    public String toString() {
        return "{" +
                " allRankings='" + getAllRankings() + "'" +
                ", calculatedRanking='" + getCalculatedRanking() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof OffloadRankingInputDto)) {
            return false;
        }
        OffloadRankingInputDto offloadRankingInputDto = (OffloadRankingInputDto) o;
        return Objects.equals(allRankings, offloadRankingInputDto.allRankings)
                && calculatedRanking == offloadRankingInputDto.calculatedRanking;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allRankings, calculatedRanking);
    }

}
