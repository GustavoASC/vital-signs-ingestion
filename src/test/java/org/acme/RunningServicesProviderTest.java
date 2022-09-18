package org.acme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RunningServicesProviderTest {

        @InjectMocks
        RunningServicesProviderImpl runningServicesProviderImpl;

        @Test
        public void shouldRemoveNonExistingExecutionId() {

                runningServicesProviderImpl.executionFinished(UUID.randomUUID());
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEmpty();
        }

        @Test
        public void shouldRetrieveRankingAfterTheyAreStoredButRemovingWithId() {


                var first = runningServicesProviderImpl.executionStarted("body-temperature-monitor", 1);
                var second = runningServicesProviderImpl.executionStarted("body-temperature-monitor", 7);
                var third = runningServicesProviderImpl.executionStarted("body-temperature-monitor", 9);
                var fourth = runningServicesProviderImpl.executionStarted("bar-function", 15);
                var fifth = runningServicesProviderImpl.executionStarted("bar-function", 2);

                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(1, 7, 9, 15, 2));

                runningServicesProviderImpl.executionFinished(first);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(7, 9, 15, 2));

                runningServicesProviderImpl.executionFinished(second);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(9, 15, 2));

                runningServicesProviderImpl.executionFinished(third);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(15, 2));

                runningServicesProviderImpl.executionFinished(fourth);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(2));

                runningServicesProviderImpl.executionFinished(fifth);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEmpty();

        }
}
