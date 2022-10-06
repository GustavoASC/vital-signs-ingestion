package org.acme.quickstart.offloading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.acme.quickstart.prediction.CouldNotPredictDurationException;
import org.acme.quickstart.prediction.DurationPredictor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffloadingHeuristicByDurationTest {

    @Mock
    private DurationPredictor durationPredictor;

    @InjectMocks
    private OffloadingHeuristicByDurationImpl offloadingHeuristicByDuration;

    @AfterEach
    public void afterEach() {
        verifyNoMoreInteractions(durationPredictor);
    }

    @Test
    public void shouldThrowExceptionWhenCannotPredictDurationForServiceInInterest() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(7l)))
                .thenThrow(new CouldNotPredictDurationException());
        when(durationPredictor.predictDurationInMillis(List.of(8l)))
                .thenReturn(150l);

        assertThatThrownBy(() -> offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
            List.of(8l)
        ), List.of(7l)))
        .isInstanceOf(CouldNotDetermineException.class);
    }

    @Test
    public void shouldNotOffloadWhenNoOtherServiceIsRunning() throws Throwable {

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(
                Collections.emptyList(),
                Collections.emptyList()
        )).isFalse();
    }

    @Test
    public void shouldNotCalculatePredictionForTargetServiceWhenNoOtherIsRunning() throws Throwable {

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(
                Collections.emptyList(),
                List.of(7l)
        )).isFalse();
    }

    @Test
    public void shouldNotCalculatePredictionForTargetServiceWhenOtherServicesRunButDoNotHaveHistoricalDuration() throws Throwable {

        when(durationPredictor.predictDurationInMillis(Collections.emptyList()))
            .thenThrow(new CouldNotPredictDurationException());

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(
                List.of(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                List.of(7l)
        )).isFalse();
    }

    @Test
    public void shouldNotOffloadWhenDurationIsLowerThanHalfOfServicesButAllHaveSameRanking() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(7l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(8l)))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis(List.of(9l)))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis(List.of(10l)))
                .thenReturn(149l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
                List.of(7l),
                List.of(8l),
                List.of(9l)
        ), List.of(10l)))
        .isFalse();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanHalfOfServicesButAllHaveSameRanking() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(7l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(8l)))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis(List.of(9l)))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis(List.of(10l)))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
                List.of(7l),
                List.of(8l),
                List.of(9l)
        ), List.of(10l)))
        .isTrue();
    }

    @Test
    public void shouldIgnoreServiceWhenCannotPredictItsDuration() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(7l)))
                .thenThrow(new CouldNotPredictDurationException());
        when(durationPredictor.predictDurationInMillis(List.of(8l)))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis(List.of(9l)))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis(List.of(10l)))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
                List.of(7l),
                List.of(8l),
                List.of(9l)
        ), List.of(10l)))
        .isFalse();
    }

    @Test
    public void shouldIgnoreSingleServiceWhenCannotPredictItsDuration() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(7l)))
                .thenThrow(new CouldNotPredictDurationException());

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
            List.of(7l)
        ), List.of(8l)))
        .isFalse();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanHalfOfServices() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(7l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(8l)))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis(List.of(9l)))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis(List.of(10l)))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
                List.of(7l),
                List.of(8l),
                List.of(9l)
        ), List.of(10l)))
        .isTrue();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanHalfOfServicesButRemovingDuplicatePredictions() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(10l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(15l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(20l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(25l)))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis(List.of(30l)))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis(List.of(35l)))
                .thenReturn(149l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
                List.of(10l),
                List.of(15l),
                List.of(20l),
                List.of(25l),
                List.of(30l)
        ), List.of(35l)))
        .isFalse();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanSingleRunningServiceWithSameRanking() throws Throwable {

        when(durationPredictor.predictDurationInMillis(List.of(10l)))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis(List.of(15l)))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(List.of(
                List.of(10l)
        ), List.of(15l)))
        .isTrue();
    }

}
