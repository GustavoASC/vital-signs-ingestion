package org.acme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResourcesLocatorImpl implements ResourcesLocator {

    private int usedCpu = 0;

    @Override
    public int getUsedCpuPercentage() {
        return usedCpu;
    }

    @Override
    public void updateUsedCpuPercentage(int usedCpu) {
        this.usedCpu = usedCpu;
    }

}
