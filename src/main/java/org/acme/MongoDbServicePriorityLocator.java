package org.acme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoDbServicePriorityLocator implements ServicePriorityLocator {

    @Override
    public int locate(String serviceName) {
        return 5;
    }
    
}
