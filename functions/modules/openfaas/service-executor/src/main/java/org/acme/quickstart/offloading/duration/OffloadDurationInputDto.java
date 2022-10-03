package org.acme.quickstart.offloading.duration;

import java.util.List;
import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OffloadDurationInputDto {

    private final List<PreviousDurationInputDto> previousDurations;
    private final String targetService;

    public OffloadDurationInputDto(List<PreviousDurationInputDto> previousDurations, String targetService) {
        this.previousDurations = previousDurations;
        this.targetService = targetService;
    }

    public List<PreviousDurationInputDto> getPreviousDurations() {
        return this.previousDurations;
    }

    public String getTargetService() {
        return this.targetService;
    }

    @Override
    public String toString() {
        return "{" +
                " previousDurations='" + getPreviousDurations() + "'" +
                ", targetService='" + getTargetService() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof OffloadDurationInputDto)) {
            return false;
        }
        OffloadDurationInputDto offloadDurationInputDto = (OffloadDurationInputDto) o;
        return Objects.equals(previousDurations, offloadDurationInputDto.previousDurations)
                && Objects.equals(targetService, offloadDurationInputDto.targetService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previousDurations, targetService);
    }

}
