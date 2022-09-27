package org.acme.quickstart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffloadingHeuristicByRankingTest {

        private static final int MAX_RANKING = 20;
        private static final int MIN_RANKING = 2;
        private static final List<Integer> MANY_RANKINGS = List.of(17, 13, 18, 20, 20, 20, 13, 17, 15, 17, 18, 18, 18,
                        18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 15, 15);

        @InjectMocks
        private OffloadingHeuristicByRankingImpl offloadingHeuristicByRanking;

        @Test
        public void shouldNotOffloadWhenNoOtherServiceIsRunning() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(), MAX_RANKING))
                                .isFalse();
        }

        @Test
        public void shouldNotOffloadWhenHigherThanOtherRankingsButOddAmountOfRankings() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(3, 7, 12), 9))
                                .isFalse();
        }

        @Test
        public void shouldNotOffloadWhenHigherThanOtherRankingsButOnlyOneRanking() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(7), 9))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenLowerThanOtherRankingsButOddAmountOfRankings() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(3, 10, 12), 9))
                                .isTrue();
        }

        @Test
        public void shouldOffloadWhenLowerThanOtherRankingsButOnlyOneRanking() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(10), 9))
                                .isTrue();
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanOtherRankingsButOnlySameService() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MANY_RANKINGS, MAX_RANKING))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsLowerThanHalfOfOtherRankings() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MANY_RANKINGS, 16))
                                .isTrue();
        }

        @Test
        public void shouldThrowExceptionWhenRankingTieOccurs() {
                assertThatThrownBy(() -> offloadingHeuristicByRanking.shouldOffloadVitalSigns(MANY_RANKINGS, 17))
                                .isInstanceOf(CouldNotDetermineException.class);
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanHalfOfOtherRankings() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MANY_RANKINGS, 18))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsLowerThanOtherRankingsButOnlySameService() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(MANY_RANKINGS, MIN_RANKING))
                                .isTrue();
        }

        @Test
        public void shouldNotOffloadWhenRankingIsHigherThanOtherRankings() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(16, 14, 13, 14, 15, 17), 18))
                                .isFalse();
        }

        @Test
        public void shouldOffloadWhenRankingIsHigherThanOtherRankings() throws Throwable {
                assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(List.of(16, 14, 13, 14, 15, 17), 12))
                                .isTrue();
        }

}
