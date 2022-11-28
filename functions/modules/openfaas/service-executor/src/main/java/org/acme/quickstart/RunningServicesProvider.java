package org.acme.quickstart;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public interface RunningServicesProvider {

    void executionStarted(UUID id, String service, int ranking);
    void executionFinished(UUID id);
    List<ServiceExecution> getRunningServices();
    List<Duration> getDurationsForService(String service);
    void clearDataForTests();

    default List<Integer> getRankingsForRunningServices() {
        return getRunningServices()
                .stream()
                .map(ServiceExecution::ranking)
                .collect(Collectors.toList());
    }

    public static class ServiceExecution {

        private String serviceName;
        private int ranking;

        public ServiceExecution(String serviceName, int ranking) {
            this.serviceName = serviceName;
            this.ranking = ranking;
        }

        public String serviceName() {
            return this.serviceName;
        }

        public int ranking() {
            return this.ranking;
        }

        @Override
        public String toString() {
            return "{" +
                    " serviceName='" + serviceName() + "'" +
                    ", ranking='" + ranking() + "'" +
                    "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof ServiceExecution)) {
                return false;
            }
            ServiceExecution serviceExecution = (ServiceExecution) o;
            return Objects.equals(serviceName, serviceExecution.serviceName) && ranking == serviceExecution.ranking;
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(serviceName, ranking);
        }

    }

}
