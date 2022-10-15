package org.acme.quickstart.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class HostnameProviderImplTest {

    @Inject
    HostnameProviderImpl hostnameProvider;

    @Test
    public void shouldLocateAliasForMapping() {
        assertThat(hostnameProvider.getHostname())
            .isEqualTo("foo");
    }

}
