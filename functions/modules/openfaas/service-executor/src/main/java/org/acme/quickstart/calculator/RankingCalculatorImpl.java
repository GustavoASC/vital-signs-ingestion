package org.acme.quickstart.calculator;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RankingCalculatorImpl implements RankingCalculator {

    private final ServicePriorityLocator servicePriorityLocator;

    public RankingCalculatorImpl(ServicePriorityLocator servicePriorityLocator) {
        this.servicePriorityLocator = servicePriorityLocator;
    }

    public int calculate(int userPriority, String serviceName) {
        return userPriority + servicePriorityLocator.locate(serviceName);
    }

}
