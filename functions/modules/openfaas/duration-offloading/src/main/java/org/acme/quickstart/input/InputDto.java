package org.acme.quickstart.input;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@RegisterForReflection
public class InputDto {

    private final List<List<Long>> durationsRunningServices;
    private final List<Long> durationsTargetService;

    public InputDto(List<List<Long>> durationsRunningServices, List<Long> durationsTargetService) {
        this.durationsRunningServices = durationsRunningServices;
        this.durationsTargetService = durationsTargetService;
    }

    public List<List<Long>> getDurationsRunningServices() {
        return this.durationsRunningServices;
    }

    public List<Long> getDurationsTargetService() {
        return this.durationsTargetService;
    }

    @Override
    public String toString() {
        return "{" +
                " durationsRunningServices='" + getDurationsRunningServices() + "'" +
                ", durationsTargetService='" + getDurationsTargetService() + "'" +
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
        return Objects.equals(durationsRunningServices, inputDto.durationsRunningServices)
                && Objects.equals(durationsTargetService, inputDto.durationsTargetService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationsRunningServices, durationsTargetService);
    }

}
