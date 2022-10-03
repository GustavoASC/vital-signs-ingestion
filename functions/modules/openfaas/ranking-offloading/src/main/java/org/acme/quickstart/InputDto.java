package org.acme.quickstart;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@RegisterForReflection
public class InputDto {

    private final List<Integer> allRankings;
    private final int calculatedRanking;

    public InputDto(List<Integer> allRankings, int calculatedRanking) {
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

}
