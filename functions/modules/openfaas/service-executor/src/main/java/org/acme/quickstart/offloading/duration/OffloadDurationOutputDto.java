package org.acme.quickstart.offloading.duration;

import java.util.Objects;

import org.acme.quickstart.offloading.shared.OffloadingDecision;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OffloadDurationOutputDto {

    private final String offloadingDecision;

    @JsonCreator
    public OffloadDurationOutputDto(String offloadingDecision) {
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
        if (!(o instanceof OffloadDurationOutputDto)) {
            return false;
        }
        OffloadDurationOutputDto offloadRankingOutputDto = (OffloadDurationOutputDto) o;
        return Objects.equals(offloadingDecision, offloadRankingOutputDto.offloadingDecision);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(offloadingDecision);
    }
}
