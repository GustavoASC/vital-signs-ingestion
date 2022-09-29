package org.acme.quickstart;

import java.util.Objects;

public class ServiceExecution {

    private final String serviceName;
    private final int ranking;

    public ServiceExecution(String serviceName, int ranking) {
        this.serviceName = serviceName;
        this.ranking = ranking;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public int getRanking() {
        return this.ranking;
    }

    @Override
    public String toString() {
        return "{" +
                " serviceName='" + getServiceName() + "'" +
                ", ranking='" + getRanking() + "'" +
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
