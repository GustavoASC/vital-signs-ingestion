package org.acme.quickstart.input;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@RegisterForReflection
public class InputDto {

    private final List<PreviousServiceDuration> previousDurations;
    private final String targetService;

    public InputDto(List<PreviousServiceDuration> previousDurations, String targetService) {
        this.previousDurations = previousDurations;
        this.targetService = targetService;
    }

    public List<PreviousServiceDuration> getPreviousDurations() {
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
        if (!(o instanceof InputDto)) {
            return false;
        }
        InputDto inputDto = (InputDto) o;
        return Objects.equals(previousDurations, inputDto.previousDurations)
                && Objects.equals(targetService, inputDto.targetService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previousDurations, targetService);
    }

}
