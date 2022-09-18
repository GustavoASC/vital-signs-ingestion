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

        private static final int USER_PRIORITY = 9;
        private static final int MAX_RANKING = 20;
        private static final int MIN_RANKING = 2;
        private static final List<Integer> MANY_RANKINGS = List.of(17, 13, 18, 20, 20, 20, 13, 17, 15, 17, 18, 18, 18,
                        18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 15, 15);
        private static final String FOO_SERVICE = "foo-service";

        @Mock
        private RankingCalculator rankingCalculator;

        @Mock
        private RunningServicesProvider runningServicesInformationProvider;

        @InjectMocks
        private OffloadingHeuristicByRankingImpl offloadingHeuristicByRanking;

        @AfterEach
        public void after() {
                verify(rankingCalculator, times(1))
                                .calculate(USER_PRIORITY, FOO_SERVICE);
                verify(runningServicesInformationProvider, times(1))
                                .getRankingsForRunningSerices();
        }

        @Test
        public void shouldNotOffloadWhenNoOtherServiceIsRunning() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(MAX_RANKING);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of());

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isFalse();
        }

        @Test
        public void shouldNotOffloadWhenHigherThanOtherRankingsButOddAmountOfRankings() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(9);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of(3, 7, 12));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isFalse();
        }

        @Test
        public void shouldNotOffloadWhenHigherThanOtherRankingsButOnlyOneRanking() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(9);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of(7));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenLowerThanOtherRankingsButOddAmountOfRankings() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(9);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of(3, 10, 12));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isTrue();
        }

        @Test
        public void shouldOffloadWhenLowerThanOtherRankingsButOnlyOneRanking() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(9);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of(10));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isTrue();
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanOtherRankingsButOnlySameService() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(MAX_RANKING);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsLowerThanHalfOfOtherRankings() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(16);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isTrue();
        }

        @Test
        public void shouldThrowExceptionWhenRankingTieOccurs() {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(17);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(MANY_RANKINGS);

                assertThatThrownBy(() -> offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isInstanceOf(CouldNotDetermineException.class);
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanHalfOfOtherRankings() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(18);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsLowerThanOtherRankingsButOnlySameService() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(MIN_RANKING);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(MANY_RANKINGS);

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isTrue();

                verify(rankingCalculator, times(1))
                                .calculate(USER_PRIORITY, FOO_SERVICE);
                verify(runningServicesInformationProvider, times(1))
                                .getRankingsForRunningSerices();
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanOtherRankings() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(18);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of(16, 14, 13, 14, 15, 17));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsHigherThanOtherRankings() throws Throwable {
                when(rankingCalculator.calculate(USER_PRIORITY, FOO_SERVICE))
                                .thenReturn(12);
                when(runningServicesInformationProvider.getRankingsForRunningSerices())
                                .thenReturn(List.of(16, 14, 13, 14, 15, 17));

                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, FOO_SERVICE))
                                .isTrue();
        }

}
