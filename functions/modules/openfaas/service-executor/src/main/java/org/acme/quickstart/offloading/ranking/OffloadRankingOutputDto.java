package org.acme.quickstart.offloading.ranking;

import java.util.Objects;

import org.acme.quickstart.offloading.shared.OffloadingDecision;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OffloadRankingOutputDto {

    private final String offloadingDecision;

    @JsonCreator
    public OffloadRankingOutputDto(String offloadingDecision) {
        this.offloadingDecision = offloadingDecision;
    }

    public OffloadingDecision getOffloadingDecision() {
        return OffloadingDecision.valueOf(this.offloadingDecision);
    }

    @Override
    public String toString() {
        return "{" +
                " offloadingDecision='" + getOffloadingDecision() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof OffloadRankingOutputDto)) {
            return false;
        }
        OffloadRankingOutputDto offloadRankingOutputDto = (OffloadRankingOutputDto) o;
        return Objects.equals(offloadingDecision, offloadRankingOutputDto.offloadingDecision);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(offloadingDecision);
    }

}
