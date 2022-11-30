package org.acme.quickstart.metrics;

import java.math.BigDecimal;
import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Metrics {

    public int userPriority;
    public int ranking;
    public BigDecimal usedCpu;
    public BigDecimal lastCpuObservation;
    public BigDecimal usedMem;
    public BigDecimal lastMemObservation;
    public long cpuCollectionTimestamp;
    public String function;
    public boolean offloading;
    public boolean runningLocally;
    public boolean exceededCriticalCpuThreshold;
    public boolean exceededCriticalMemThreshold;
    public boolean triggeredHeuristicByRanking;
    public boolean resultForHeuristicByRanking;
    public boolean triggeredHeuristicByDuration;
    public boolean resultForHeuristicByDuration;
    public boolean assumingFallbackForHeuristics;

    @Override
    public String toString() {
        return "{" +
                " userPriority='" + userPriority + "'" +
                ", ranking='" + ranking + "'" +
                ", usedCpu='" + usedCpu + "'" +
                ", lastCpuObservation='" + lastCpuObservation + "'" +
                ", usedMem='" + usedMem + "'" +
                ", lastMemObservation='" + lastMemObservation + "'" +
                ", cpuCollectionTimestamp='" + cpuCollectionTimestamp + "'" +
                ", function='" + function + "'" +
                ", offloading='" + offloading + "'" +
                ", runningLocally='" + runningLocally + "'" +
                ", exceededCriticalCpuThreshold='" + exceededCriticalCpuThreshold + "'" +
                ", exceededCriticalMemThreshold='" + exceededCriticalMemThreshold + "'" +
                ", triggeredHeuristicByRanking='" + triggeredHeuristicByRanking + "'" +
                ", resultForHeuristicByRanking='" + resultForHeuristicByRanking + "'" +
                ", triggeredHeuristicByDuration='" + triggeredHeuristicByDuration + "'" +
                ", resultForHeuristicByDuration='" + resultForHeuristicByDuration + "'" +
                ", assumingFallbackForHeuristics='" + assumingFallbackForHeuristics + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Metrics)) {
            return false;
        }
        Metrics metrics = (Metrics) o;
        return userPriority == metrics.userPriority && ranking == metrics.ranking
                && Objects.equals(usedCpu, metrics.usedCpu)
                && Objects.equals(lastCpuObservation, metrics.lastCpuObservation)
                && Objects.equals(usedMem, metrics.usedMem)
                && Objects.equals(lastMemObservation, metrics.lastMemObservation)
                && cpuCollectionTimestamp == metrics.cpuCollectionTimestamp
                && Objects.equals(function, metrics.function) && offloading == metrics.offloading
                && runningLocally == metrics.runningLocally
                && exceededCriticalCpuThreshold == metrics.exceededCriticalCpuThreshold
                && exceededCriticalMemThreshold == metrics.exceededCriticalMemThreshold
                && triggeredHeuristicByRanking == metrics.triggeredHeuristicByRanking
                && resultForHeuristicByRanking == metrics.resultForHeuristicByRanking
                && triggeredHeuristicByDuration == metrics.triggeredHeuristicByDuration
                && resultForHeuristicByDuration == metrics.resultForHeuristicByDuration
                && assumingFallbackForHeuristics == metrics.assumingFallbackForHeuristics;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPriority, ranking, usedCpu, lastCpuObservation, usedMem, lastMemObservation,
                cpuCollectionTimestamp, function, offloading, runningLocally, exceededCriticalCpuThreshold,
                exceededCriticalMemThreshold, triggeredHeuristicByRanking, resultForHeuristicByRanking,
                triggeredHeuristicByDuration, resultForHeuristicByDuration, assumingFallbackForHeuristics);
    }

}
