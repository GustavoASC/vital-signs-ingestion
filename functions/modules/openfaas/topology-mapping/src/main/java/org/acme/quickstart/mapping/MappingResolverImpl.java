package org.acme.quickstart.mapping;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.acme.quickstart.restclient.MappingProviderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MappingResolverImpl implements MappingResolver {

    private final MappingProviderClient mappingProviderClient;
    private final HostnameProvider hostnameProvider;

    public MappingResolverImpl(
            @RestClient MappingProviderClient mappingProviderClient,
            HostnameProvider hostnameProvider) {
        this.mappingProviderClient = mappingProviderClient;
        this.hostnameProvider = hostnameProvider;
    }

    @Override
    public String resolveMappingForCurrentHostname() {

        try {
            String hostname = hostnameProvider.getHostname();
            String mapping = mappingProviderClient.getMappingProperties();

            Properties properties = new Properties();
            properties.load(new StringReader(mapping));

            String connectedAlias = Optional
                    .ofNullable(properties.getProperty("connection." + hostname))
                    .orElseThrow(() -> new IllegalStateException("Could not find connection for " + hostname));

            String ipAddressForConnectedAlias = Optional
                    .ofNullable(properties.getProperty("ip_address." + connectedAlias))
                    .orElseThrow(() -> new IllegalStateException("Could not find IP address for " + connectedAlias));

            return ipAddressForConnectedAlias;

        } catch (IOException e) {
            throw new IllegalStateException("Should never throw IOException because loaded from StringReader");
        }
    }

}
