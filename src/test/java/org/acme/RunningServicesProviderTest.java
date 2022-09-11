package org.acme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RunningServicesProviderTest {

    @InjectMocks
    RunningServicesProviderImpl runningServicesProviderImpl;

    @Test
    public void shouldRetrieveRankingAfterTheyAreStored() {

        runningServicesProviderImpl.addRunningService("foo-function", 1);
        runningServicesProviderImpl.addRunningService("foo-function", 7);
        runningServicesProviderImpl.addRunningService("foo-function", 9);
        runningServicesProviderImpl.addRunningService("bar-function", 15);
        runningServicesProviderImpl.addRunningService("bar-function", 2);

        assertThat(runningServicesProviderImpl.provideRankings())
                .isEqualTo(Map.of("foo-function", List.of(1, 7, 9), "bar-function", List.of(15, 2)));

    }
}
