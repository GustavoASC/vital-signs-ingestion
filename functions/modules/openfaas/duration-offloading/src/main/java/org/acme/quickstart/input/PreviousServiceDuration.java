package org.acme.quickstart.input;

import java.util.List;
import java.util.Objects;

public class PreviousServiceDuration {

    private final String name;
    private final List<Long> durations;

    public PreviousServiceDuration(String name, List<Long> durations) {
        this.name = name;
        this.durations = durations;
    }

    public String getName() {
        return this.name;
    }

    public List<Long> getDurations() {
        return this.durations;
    }

    @Override
    public String toString() {
        return "{" +
                " name='" + getName() + "'" +
                ", durations='" + getDurations() + "'" +
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
        return Objects.equals(name, previousServiceDuration.name)
                && Objects.equals(durations, previousServiceDuration.durations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, durations);
    }

}
