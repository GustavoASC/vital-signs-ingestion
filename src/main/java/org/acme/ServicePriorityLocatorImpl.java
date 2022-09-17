package org.acme;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePriorityLocatorImpl implements ServicePriorityLocator {

    private static final Map<String, Integer> SERVICE_PRIORITIES = Map.of("body-temperature-monitor", 4, "bar-function", 6);
    private static final int DEFAULT_PRIORITY = 5;

    @Override
    public int locate(String serviceName) {
        return SERVICE_PRIORITIES.getOrDefault(serviceName, DEFAULT_PRIORITY);
    }

}
