package org.acme.quickstart.results;

import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Results {

    public long initialServiceTimestamp;
    public long endTimestamp;

    @Override
    public String toString() {
        return "{" +
                " initialServiceTimestamp='" + initialServiceTimestamp + "'" +
                ", endTimestamp='" + endTimestamp + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Results)) {
            return false;
        }
        Results results = (Results) o;
        return initialServiceTimestamp == results.initialServiceTimestamp && endTimestamp == results.endTimestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialServiceTimestamp, endTimestamp);
    }

}
