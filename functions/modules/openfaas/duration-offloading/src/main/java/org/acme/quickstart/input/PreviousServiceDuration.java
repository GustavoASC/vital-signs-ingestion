package org.acme.quickstart.input;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PreviousServiceDuration {

    private final List<Long> durations;

    @JsonCreator
    public PreviousServiceDuration(List<Long> durations) {
        this.durations = durations;
    }

    public List<Long> getDurations() {
        return this.durations;
    }

    @Override
    public String toString() {
        return "{" +
                " durations='" + getDurations() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PreviousServiceDuration)) {
            return false;
        }
        PreviousServiceDuration previousServiceDuration = (PreviousServiceDuration) o;
        return Objects.equals(durations, previousServiceDuration.durations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(durations);
    }

}
