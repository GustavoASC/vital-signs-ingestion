package org.acme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RunningServicesProviderTest {

        @InjectMocks
        RunningServicesProviderImpl runningServicesProviderImpl;

        @Test
        public void shouldRemoveRankingFromNonExistingService() {

                runningServicesProviderImpl.executionFinished("non-existing", 7);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEmpty();
        }

        @Test
        public void shouldRemoveNonExistingRankingFroExistingService() {

                runningServicesProviderImpl.executionStarted("body-temperature-monitor", 1);
                runningServicesProviderImpl.executionFinished("body-temperature-monitor", 7);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(1));
        }

        @Test
        public void shouldRetrieveRankingAfterTheyAreStored() {


                runningServicesProviderImpl.executionStarted("body-temperature-monitor", 1);
                runningServicesProviderImpl.executionStarted("body-temperature-monitor", 7);
                runningServicesProviderImpl.executionStarted("body-temperature-monitor", 9);
                runningServicesProviderImpl.executionStarted("bar-function", 15);
                runningServicesProviderImpl.executionStarted("bar-function", 2);

                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(1, 7, 9, 15, 2));

                runningServicesProviderImpl.executionFinished("body-temperature-monitor", 1);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(7, 9, 15, 2));

                runningServicesProviderImpl.executionFinished("body-temperature-monitor", 7);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(9, 15, 2));

                runningServicesProviderImpl.executionFinished("body-temperature-monitor", 9);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(15, 2));

                runningServicesProviderImpl.executionFinished("bar-function", 15);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEqualTo(List.of(2));

                runningServicesProviderImpl.executionFinished("bar-function", 2);
                assertThat(runningServicesProviderImpl.getRankingsForRunningSerices())
                                .isEmpty();

        }
}
