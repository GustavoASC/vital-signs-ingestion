package org.acme.quickstart.resources;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourcesLocatorResponse {

    private final BigDecimal usedCpu;
    private final BigDecimal lastCpuObservation;

    public ResourcesLocatorResponse(BigDecimal usedCpu, BigDecimal lastCpuObservation) {
        this.usedCpu = usedCpu;
        this.lastCpuObservation = lastCpuObservation;
    }

    public BigDecimal getUsedCpu() {
        return this.usedCpu;
    }

    public BigDecimal getLastCpuObservation() {
        return this.lastCpuObservation;
    }

    @Override
    public String toString() {
        return "{" +
                " usedCpu='" + getUsedCpu() + "'" +
                ", lastCpuObservation='" + getLastCpuObservation() + "'" +
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
                && Objects.equals(lastCpuObservation, resourcesLocatorResponse.lastCpuObservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usedCpu, lastCpuObservation);
    }

}
