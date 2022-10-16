package org.acme.quickstart.mapping;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HostnameProviderImpl implements HostnameProvider {

    private final String alias;

    public HostnameProviderImpl(@ConfigProperty(name = "mapping.alias-current-machine") String alias) {
        this.alias = alias;
    }

    @Override
    public String getHostname() {
        return this.alias;
    }

}
