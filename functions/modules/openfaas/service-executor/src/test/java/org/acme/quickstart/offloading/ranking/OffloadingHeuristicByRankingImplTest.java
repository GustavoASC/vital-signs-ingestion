package org.acme.quickstart.offloading.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.acme.quickstart.RunningServicesProvider;
import org.acme.quickstart.offloading.shared.CouldNotDetermineException;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffloadingHeuristicByRankingImplTest {

    private final int TEST_RANKING = 2;
    
    @Mock
    RunningServicesProvider servicesProvider;

    @Mock
    ServerlessFunctionClient serverlessFunctionClient;

    @InjectMocks
    OffloadingHeuristicByRankingImpl offloadingHeuristicByRanking;

    @Test
    public void shouldInvokeOffloadingHeuristicFunctionwithRunLocallyAsDecision() throws Throwable {

        when(servicesProvider.getRankingsForRunningServices())
            .thenReturn(Collections.emptyList());
        when(serverlessFunctionClient.runRankingOffloading("ranking-offloading",
            new OffloadRankingInputDto(Collections.emptyList(), TEST_RANKING))
        ).thenReturn(new OffloadRankingOutputDto("RUN_LOCALLY"));

        assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(TEST_RANKING))
            .isFalse();
    }

    @Test
    public void shouldInvokeOffloadingHeuristicFunctionWithOffloadAsDecision() throws Throwable {

        List<Integer> rankings = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        when(servicesProvider.getRankingsForRunningServices())
            .thenReturn(rankings);
        when(serverlessFunctionClient.runRankingOffloading("ranking-offloading",
            new OffloadRankingInputDto(rankings, TEST_RANKING))
        ).thenReturn(new OffloadRankingOutputDto("OFFLOAD"));

        assertThat(offloadingHeuristicByRanking.shouldOffloadVitalSigns(TEST_RANKING))
            .isTrue();
    }

    @Test
    public void shouldInvokeOffloadingHeuristicFunctionWithUnknownAsDecision() throws Throwable {

        List<Integer> rankings = List.of(1, 2, 3);

        when(servicesProvider.getRankingsForRunningServices())
            .thenReturn(rankings);
        when(serverlessFunctionClient.runRankingOffloading("ranking-offloading",
            new OffloadRankingInputDto(rankings, TEST_RANKING))
        ).thenReturn(new OffloadRankingOutputDto("UNKNOWN"));

        assertThatThrownBy(() -> offloadingHeuristicByRanking.shouldOffloadVitalSigns(TEST_RANKING))
            .isInstanceOf(CouldNotDetermineException.class);
    }

}
