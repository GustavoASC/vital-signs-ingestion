package org.acme.quickstart.resources;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MachineResourcesOutputDto {

    private final BigDecimal cpu;
    private final BigDecimal lastObservation;

    @JsonCreator
    public MachineResourcesOutputDto(BigDecimal cpu, BigDecimal lastObservation) {
        this.cpu = cpu;
        this.lastObservation = lastObservation;
    }

    public BigDecimal getCpu() {
        return this.cpu;
    }

    public BigDecimal getLastObservation() {
        return this.lastObservation;
    }

    @Override
    public String toString() {
        return "{" +
                " cpu='" + getCpu() + "'" +
                ", lastObservation='" + getLastObservation() + "'" +
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
                && Objects.equals(lastObservation, machineResourcesOutputDto.lastObservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpu, lastObservation);
    }

}
