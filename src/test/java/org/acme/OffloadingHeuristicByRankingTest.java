package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffloadingHeuristicByRankingTest {

        private static final int MAX_RANKING = 20;
        private static final int MIN_RANKING = 2;
        private static final List<Integer> MANY_RANKINGS = List.of(17, 13, 18, 20, 20, 20, 13, 17, 15, 17, 18, 18, 18,
                        18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 15, 15);

        @Mock
        private RunningServicesProvider runningServicesInformationProvider;

        @InjectMocks
        private OffloadingHeuristicByRankingImpl offloadingHeuristicByRanking;

        @AfterEach
        public void after() {
                verify(runningServicesInformationProvider, times(1))
                                .getRankingsForRunningServices();
        }

        @Test
        public void shouldNotOffloadWhenNoOtherServiceIsRunning() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of());

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MAX_RANKING))
                                .isFalse();
        }

        @Test
        public void shouldNotOffloadWhenHigherThanOtherRankingsButOddAmountOfRankings() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of(3, 7, 12));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(9))
                                .isFalse();
        }

        @Test
        public void shouldNotOffloadWhenHigherThanOtherRankingsButOnlyOneRanking() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of(7));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(9))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenLowerThanOtherRankingsButOddAmountOfRankings() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of(3, 10, 12));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(9))
                                .isTrue();
        }

        @Test
        public void shouldOffloadWhenLowerThanOtherRankingsButOnlyOneRanking() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of(10));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(9))
                                .isTrue();
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanOtherRankingsButOnlySameService() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MAX_RANKING))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsLowerThanHalfOfOtherRankings() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(16))
                                .isTrue();
        }

        @Test
        public void shouldThrowExceptionWhenRankingTieOccurs() {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(MANY_RANKINGS);

                assertThatThrownBy(() -> offloadingHeuristicByRanking.shouldOffloadVitalSigns(17))
                                .isInstanceOf(CouldNotDetermineException.class);
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanHalfOfOtherRankings() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(18))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsLowerThanOtherRankingsButOnlySameService() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MIN_RANKING))
                                .isTrue();

                verify(runningServicesInformationProvider, times(1))
                                .getRankingsForRunningServices();
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanOtherRankings() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of(16, 14, 13, 14, 15, 17));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(18))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsHigherThanOtherRankings() throws Throwable {
                when(runningServicesInformationProvider.getRankingsForRunningServices())
                                .thenReturn(List.of(16, 14, 13, 14, 15, 17));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(12))
                                .isTrue();
        }

}
