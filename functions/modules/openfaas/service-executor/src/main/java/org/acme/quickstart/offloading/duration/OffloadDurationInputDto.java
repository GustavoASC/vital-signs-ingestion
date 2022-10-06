package org.acme.quickstart.offloading.duration;

import java.util.List;
import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OffloadDurationInputDto {

    private final List<List<Long>> durationsRunningServices;
    private final List<Long> durationsTargetService;

    public OffloadDurationInputDto(List<List<Long>> durationsRunningServices, List<Long> durationsTargetService) {
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
        if (!(o instanceof OffloadDurationInputDto)) {
            return false;
        }
        OffloadDurationInputDto offloadDurationInputDto = (OffloadDurationInputDto) o;
        return Objects.equals(durationsRunningServices, offloadDurationInputDto.durationsRunningServices)
                && Objects.equals(durationsTargetService, offloadDurationInputDto.durationsTargetService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationsRunningServices, durationsTargetService);
    }

}
