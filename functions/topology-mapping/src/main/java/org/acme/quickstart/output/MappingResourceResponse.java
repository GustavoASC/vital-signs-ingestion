package org.acme.quickstart.output;

import java.util.Objects;

public class MappingResourceResponse {

    private final String mappedDestination;

    public MappingResourceResponse(String mappedDestination) {
        this.mappedDestination = mappedDestination;
    }

    public String getMappedDestination() {
        return this.mappedDestination;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MappingResourceResponse)) {
            return false;
        }
        MappingResourceResponse mappingResourceResponse = (MappingResourceResponse) o;
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
