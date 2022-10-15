package org.acme.quickstart.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.acme.quickstart.restclient.MappingProviderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MappingResolverImplTest {

    @Mock
    HostnameProvider hostnameProvider;

    @Mock
    MappingProviderClient mappingProviderClient;

    @InjectMocks
    MappingResolverImpl mappingResolverImpl;

    @ParameterizedTest
    @MethodSource(value = {"aliasResolvingToNumericalIpAddress", "aliasResolvingToHttpIpAddress"})
    public void shouldDetectNumericalIpAddressForGivenHostname(String alias, String ipAddress) throws Throwable {

        when(hostnameProvider.getHostname())
            .thenReturn(alias);

        when(mappingProviderClient.getMappingProperties())
            .thenReturn(textFromResource("nodes-mappings.properties"));

        assertThat(mappingResolverImpl.resolveMappingForCurrentHostname())
            .isEqualTo(ipAddress);
    }

    private static Stream<Arguments> aliasResolvingToNumericalIpAddress() {
        return Stream.of(Arguments.of("edge_node_a", "19.212.193.202"));
    }

    private static Stream<Arguments> aliasResolvingToHttpIpAddress() {
        return Stream.of(Arguments.of("fog_node_c", "https://hxshiaussindnadiu1dhasid83hfe82.lambda-url.sa-east-1.on.aws"));
    }

    @ParameterizedTest
    @MethodSource(value = {"aliasWithoutMapping", "connectedAliasWithoutIp"})
    public void shouldThrowIllegalStateExceptionWhenMappingDoesNotExist(String alias, String exceptionMessage) throws Throwable {
        when(hostnameProvider.getHostname())
            .thenReturn(alias);

        when(mappingProviderClient.getMappingProperties())
            .thenReturn(textFromResource("nodes-mappings.properties"));

        assertThatThrownBy(() -> mappingResolverImpl.resolveMappingForCurrentHostname())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(exceptionMessage);
    }

    private static Stream<Arguments> aliasWithoutMapping() {
        return Stream.of(Arguments.of("unmapped_alias", "find connection for unmapped_alias"));
    }

    private static Stream<Arguments> connectedAliasWithoutIp() {
        return Stream.of(Arguments.of("edge_node_c", "find IP address for mapped-alias-but-without-ip-address"));
    }

    @Test
    public void shouldThrowExceptionRegardingConnectionNotFoundWhenPropertiesFileIsIvalid() {

        when(hostnameProvider.getHostname())
            .thenReturn("foo");

        when(mappingProviderClient.getMappingProperties())
            .thenReturn("invalid-content-for-properties-file");

        assertThatThrownBy(() -> mappingResolverImpl.resolveMappingForCurrentHostname())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("find connection for foo");
    }

    private String textFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/mapping-resolver", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}
