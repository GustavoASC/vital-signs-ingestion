package org.acme.quickstart.resources;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourcesLocatorResponse {

    private final BigDecimal usedCpu;
    private final BigDecimal lastCpuObservation;
    private final BigDecimal usedMemory;
    private final BigDecimal lastMemoryObservation;

    public ResourcesLocatorResponse(BigDecimal usedCpu, BigDecimal lastCpuObservation) {
        this(usedCpu, lastCpuObservation, null, null);
    }

    public ResourcesLocatorResponse(
            BigDecimal usedCpu,
            BigDecimal lastCpuObservation,
            BigDecimal usedMemory,
            BigDecimal lastMemoryObservation) {
        this.usedCpu = usedCpu;
        this.lastCpuObservation = lastCpuObservation;
        this.usedMemory = usedMemory;
        this.lastMemoryObservation = lastMemoryObservation;
    }

    public BigDecimal getUsedCpu() {
        return this.usedCpu;
    }

    public BigDecimal getLastCpuObservation() {
        return this.lastCpuObservation;
    }

    public BigDecimal getUsedMemory() {
        return this.usedMemory;
    }

    public BigDecimal getLastMemoryObservation() {
        return this.lastMemoryObservation;
    }

    @Override
    public String toString() {
        return "{" +
                " usedCpu='" + getUsedCpu() + "'" +
                ", lastCpuObservation='" + getLastCpuObservation() + "'" +
                ", usedMemory='" + getUsedMemory() + "'" +
                ", lastMemoryObservation='" + getLastMemoryObservation() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ResourcesLocatorResponse)) {
            return false;
        }
        ResourcesLocatorResponse resourcesLocatorResponse = (ResourcesLocatorResponse) o;
        return Objects.equals(usedCpu, resourcesLocatorResponse.usedCpu)
                && Objects.equals(lastCpuObservation, resourcesLocatorResponse.lastCpuObservation)
                && Objects.equals(usedMemory, resourcesLocatorResponse.usedMemory)
                && Objects.equals(lastMemoryObservation, resourcesLocatorResponse.lastMemoryObservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usedCpu, lastCpuObservation, usedMemory, lastMemoryObservation);
    }

}
