package org.acme;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ResourcesInputDto {

    private final String hostname;
    private final Integer cpu;

    public ResourcesInputDto(String hostname, Integer cpu) {
        this.hostname = hostname;
        this.cpu = cpu;
    }

    public String getHostname() {
        return this.hostname;
    }

    public Integer getCpu() {
        return this.cpu;
    }

    @Override
    public String toString() {
        return "{" +
                " hostname='" + getHostname() + "'" +
                ", cpu='" + getCpu() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ResourcesInputDto)) {
            return false;
        }
        ResourcesInputDto resourcesInputDto = (ResourcesInputDto) o;
        return Objects.equals(hostname, resourcesInputDto.hostname)
                && Objects.equals(cpu, resourcesInputDto.cpu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, cpu);
    }

}
