package org.acme.quickstart.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RankingCalculatorTest {

    @Mock
    private ServicePriorityLocator servicePriorityLocator;

    @InjectMocks
    private RankingCalculatorImpl calculator;

    @Test
    public void shouldCalculateRankingForUserWithLowPriorityAndService() {
        when(servicePriorityLocator.locate("foo"))
                .thenReturn(6);

        assertThat(calculator.calculate(1, "foo"))
                .isEqualTo(8);

        verify(servicePriorityLocator, times(1))
                .locate(any());
    }

    @Test
    public void shouldCalculateRankingForUserWithHighPriorityAndService() {
        when(servicePriorityLocator.locate("foo"))
                .thenReturn(6);

        assertThat(calculator.calculate(5, "foo"))
                .isEqualTo(16);

        verify(servicePriorityLocator, times(1))
                .locate(any());
    }

}
