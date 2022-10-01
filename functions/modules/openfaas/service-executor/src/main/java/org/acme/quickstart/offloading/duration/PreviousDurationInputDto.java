package org.acme.quickstart.offloading.duration;

import java.util.List;
import java.util.Objects;

public class PreviousDurationInputDto {

    private final String name;
    private final List<Long> durations;

    public PreviousDurationInputDto(String name, List<Long> durations) {
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
        if (!(o instanceof PreviousDurationInputDto)) {
            return false;
        }
        PreviousDurationInputDto previousDurationInputDto = (PreviousDurationInputDto) o;
        return Objects.equals(name, previousDurationInputDto.name)
                && Objects.equals(durations, previousDurationInputDto.durations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, durations);
    }

}
