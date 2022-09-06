package org.acme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RankingCalculatorTest {

    private RankingCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new RankingCalculator(service -> 6);
    }

    @Test
    public void shouldCalculateRankingForUserAndService() {
        assertThat(calculator.calculate(1, "foo"))
            .isEqualTo(7);
    }
    
}
