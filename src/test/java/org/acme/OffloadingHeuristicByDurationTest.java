package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.acme.RunningServicesProvider.ServiceExecution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffloadingHeuristicByDurationTest {

    @Mock
    private RunningServicesProvider servicesProvider;

    @Mock
    private DurationPredictor durationPredictor;

    @InjectMocks
    private OffloadingHeuristicByDurationImpl offloadingHeuristicByDuration;

    @AfterEach
    public void afterEach() {
        verifyNoMoreInteractions(servicesProvider, durationPredictor);
    }

    @Test
    public void shouldThrowExceptionWhenCannotPredictDurationForServiceInInterest() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(new ServiceExecution("bar-service", 7)));

        when(durationPredictor.predictDurationInMillis("bar-service"))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenThrow(new CouldNotPredictDurationException());

        assertThatThrownBy(() -> offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isInstanceOf(CouldNotDetermineException.class);
    }

    @Test
    public void shouldNotOffloadWhenNoOtherServiceIsRunning() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(Collections.emptyList());

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isFalse();
    }

    @Test
    public void shouldNotOffloadWhenDurationIsLowerThanHalfOfServicesButAllHaveSameRanking() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("def-service", 7),
                        new ServiceExecution("ghi-service", 7)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("def-service"))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis("ghi-service"))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(149l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isFalse();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanHalfOfServicesButAllHaveSameRanking() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("def-service", 7),
                        new ServiceExecution("ghi-service", 7)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("def-service"))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis("ghi-service"))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isTrue();
    }

    @Test
    public void shouldIgnoreServiceWhenCannotPredictItsDuration() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("def-service", 7),
                        new ServiceExecution("ghi-service", 7)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenThrow(new CouldNotPredictDurationException());
        when(durationPredictor.predictDurationInMillis("def-service"))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis("ghi-service"))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isFalse();
    }

    @Test
    public void shouldIgnoreSingleServiceWhenCannotPredictItsDuration() throws Throwable {
        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(new ServiceExecution("abc-service", 7)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenThrow(new CouldNotPredictDurationException());

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isFalse();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanHalfOfServicesFilteringSameRanking() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("def-service", 7),
                        new ServiceExecution("ghi-service", 7),
                        new ServiceExecution("yyy-service", 6),
                        new ServiceExecution("zzz-service", 8)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("def-service"))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis("ghi-service"))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isTrue();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanHalfOfServicesButRemovingDuplicatePredictions() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("def-service", 7),
                        new ServiceExecution("ghi-service", 7),
                        new ServiceExecution("jkl-service", 7),
                        new ServiceExecution("mno-service", 7),
                        new ServiceExecution("yyy-service", 6),
                        new ServiceExecution("zzz-service", 8)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("def-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("ghi-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("jkl-service"))
                .thenReturn(150l);
        when(durationPredictor.predictDurationInMillis("mno-service"))
                .thenReturn(200l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(149l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isFalse();
    }

    @Test
    public void shouldOffloadWhenDurationIsHigherThanSingleRunningServiceWithSameRanking() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(new ServiceExecution("abc-service", 7)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isTrue();
    }

    @Test
    public void shouldIgnoreSameServiceFromListOfRunningServices() throws Throwable {
        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(new ServiceExecution("foo-service", 7)));

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isFalse();
    }

    @Test
    public void shouldCalculatePredictionOnceEvenWhenServiceHasMultipleRunningInstances() throws Throwable {

        when(servicesProvider.getRunningServices())
                .thenReturn(List.of(
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("abc-service", 7),
                        new ServiceExecution("foo-service", 7)));

        when(durationPredictor.predictDurationInMillis("abc-service"))
                .thenReturn(100l);
        when(durationPredictor.predictDurationInMillis("foo-service"))
                .thenReturn(151l);

        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(7, "foo-service"))
                .isTrue();

        verify(durationPredictor, times(1))
                .predictDurationInMillis("abc-service");
        verify(durationPredictor, times(1))
                .predictDurationInMillis("foo-service");
    }

}
