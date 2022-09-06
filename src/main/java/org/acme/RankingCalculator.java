package org.acme;

public class RankingCalculator {

    private final ServicePriorityLocator servicePriorityLocator;

    public RankingCalculator(ServicePriorityLocator servicePriorityLocator) {
        this.servicePriorityLocator = servicePriorityLocator;
    }

    public int calculate(int userPriority, String serviceName) {
        return userPriority + servicePriorityLocator.locate(serviceName);
    }
    
}
