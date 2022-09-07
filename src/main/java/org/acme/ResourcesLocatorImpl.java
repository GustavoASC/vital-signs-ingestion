package org.acme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResourcesLocatorImpl implements ResourcesLocator {

    @Override
    public int usedCpuPercentage() {
        return 0;
    }

}
