package org.acme.quickstart.resources;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MachineResourcesOutputDto {

    private final BigDecimal cpu;
    private final BigDecimal lastObservation;
    private final BigDecimal memory;
    private final BigDecimal lastMemoryObservation;

    @JsonCreator
    public MachineResourcesOutputDto(BigDecimal cpu, BigDecimal lastObservation, BigDecimal memory,
            BigDecimal lastMemoryObservation) {
        this.cpu = cpu;
        this.lastObservation = lastObservation;
        this.memory = memory;
        this.lastMemoryObservation = lastMemoryObservation;
    }

    public BigDecimal getCpu() {
        return this.cpu;
    }

    public BigDecimal getLastObservation() {
        return this.lastObservation;
    }

    public BigDecimal getMemory() {
        return this.memory;
    }

    public BigDecimal getLastMemoryObservation() {
        return this.lastMemoryObservation;
    }

    @Override
    public String toString() {
        return "{" +
                " cpu='" + getCpu() + "'" +
                ", lastObservation='" + getLastObservation() + "'" +
                ", memory='" + getMemory() + "'" +
                ", lastMemoryObservation='" + getLastMemoryObservation() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MachineResourcesOutputDto)) {
            return false;
        }
        MachineResourcesOutputDto machineResourcesOutputDto = (MachineResourcesOutputDto) o;
        return Objects.equals(cpu, machineResourcesOutputDto.cpu)
                && Objects.equals(lastObservation, machineResourcesOutputDto.lastObservation)
                && Objects.equals(memory, machineResourcesOutputDto.memory)
                && Objects.equals(lastMemoryObservation, machineResourcesOutputDto.lastMemoryObservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpu, lastObservation, memory, lastMemoryObservation);
    }

}
