package org.acme.quickstart.topology;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TopologyMappingOutputDto {

    private final String mappedDestination;

    @JsonCreator
    public TopologyMappingOutputDto(String mappedDestination) {
        this.mappedDestination = mappedDestination;
    }

    public String getMappedDestination() {
        return this.mappedDestination;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TopologyMappingOutputDto)) {
            return false;
        }
        TopologyMappingOutputDto mappingResourceResponse = (TopologyMappingOutputDto) o;
        return Objects.equals(mappedDestination, mappingResourceResponse.mappedDestination);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mappedDestination);
    }

    @Override
    public String toString() {
        return "{" +
                " mappedDestination='" + getMappedDestination() + "'" +
                "}";
    }

}
