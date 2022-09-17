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
        public void shouldRemoveRankingFromNonExistingService() {

                runningServicesProviderImpl.removeRunningService("non-existing", 7);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEmpty();
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEmpty();
        }

        @Test
        public void shouldRemoveNonExistingRankingFroExistingService() {

                runningServicesProviderImpl.addRunningService("body-temperature-monitor", 1);
                runningServicesProviderImpl.removeRunningService("body-temperature-monitor", 7);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("body-temperature-monitor", List.of(1)));
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEqualTo(List.of(1));
        }

        @Test
        public void shouldRetrieveRankingAfterTheyAreStored() {

                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEmpty();

                runningServicesProviderImpl.addRunningService("body-temperature-monitor", 1);
                runningServicesProviderImpl.addRunningService("body-temperature-monitor", 7);
                runningServicesProviderImpl.addRunningService("body-temperature-monitor", 9);
                runningServicesProviderImpl.addRunningService("bar-function", 15);
                runningServicesProviderImpl.addRunningService("bar-function", 2);

                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("body-temperature-monitor", List.of(1, 7, 9), "bar-function", List.of(15, 2)));
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEqualTo(List.of(1, 7, 9, 15, 2));

                runningServicesProviderImpl.removeRunningService("body-temperature-monitor", 1);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("body-temperature-monitor", List.of(7, 9), "bar-function", List.of(15, 2)));
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEqualTo(List.of(7, 9, 15, 2));

                runningServicesProviderImpl.removeRunningService("body-temperature-monitor", 7);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("body-temperature-monitor", List.of(9), "bar-function", List.of(15, 2)));
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEqualTo(List.of(9, 15, 2));

                runningServicesProviderImpl.removeRunningService("body-temperature-monitor", 9);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("bar-function", List.of(15, 2)));
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEqualTo(List.of(15, 2));

                runningServicesProviderImpl.removeRunningService("bar-function", 15);
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEqualTo(List.of(2));

                runningServicesProviderImpl.removeRunningService("bar-function", 2);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEmpty();
                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEmpty();

        }
}
