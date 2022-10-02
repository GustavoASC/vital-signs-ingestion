package org.acme.quickstart.resources;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MachineResourcesOutputDto {

    private final Double cpu;

    @JsonCreator
    public MachineResourcesOutputDto(Double cpu) {
        this.cpu = cpu;
    }

    public Double getCpu() {
        return this.cpu;
    }

    @Override
    public String toString() {
        return "{" +
                " cpu='" + getCpu() + "'" +
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
        return Objects.equals(cpu, machineResourcesOutputDto.cpu);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cpu);
    }

}
