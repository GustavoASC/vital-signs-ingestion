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
        }

        @Test
        public void shouldRemoveNonExistingRankingFroExistingService() {

                runningServicesProviderImpl.addRunningService("foo-function", 1);
                runningServicesProviderImpl.removeRunningService("foo-function", 7);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("foo-function", List.of(1)));
        }

        @Test
        public void shouldRetrieveRankingAfterTheyAreStored() {

                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEmpty();

                runningServicesProviderImpl.addRunningService("foo-function", 1);
                runningServicesProviderImpl.addRunningService("foo-function", 7);
                runningServicesProviderImpl.addRunningService("foo-function", 9);
                runningServicesProviderImpl.addRunningService("bar-function", 15);
                runningServicesProviderImpl.addRunningService("bar-function", 2);

                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("foo-function", List.of(1, 7, 9), "bar-function", List.of(15, 2)));

                runningServicesProviderImpl.removeRunningService("foo-function", 1);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("foo-function", List.of(7, 9), "bar-function", List.of(15, 2)));

                runningServicesProviderImpl.removeRunningService("foo-function", 7);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("foo-function", List.of(9), "bar-function", List.of(15, 2)));

                runningServicesProviderImpl.removeRunningService("foo-function", 9);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEqualTo(Map.of("bar-function", List.of(15, 2)));

                runningServicesProviderImpl.removeRunningService("bar-function", 15);
                runningServicesProviderImpl.removeRunningService("bar-function", 2);
                assertThat(runningServicesProviderImpl.provideRankings())
                                .isEmpty();

        }
}
