package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DurationStoreTest {

    @InjectMocks
    private DurationStoreImpl store;

    @Test
    public void shouldNotLocateAnythingWhenNoValueIsPresent() {
        assertThat(store.getDurations("body-temperature-monitor"))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldLocateBothDurationsForSingleService() {
        store.storeDuration("body-temperature-monitor", 10);
        store.storeDuration("body-temperature-monitor", 20);
        assertThat(store.getDurations("body-temperature-monitor"))
                .isEqualTo(List.<Long>of(10l, 20l));
    }

    @Test
    public void shouldLocateOnlyDurationsForSpecificService() {
        store.storeDuration("body-temperature-monitor", 10);
        store.storeDuration("body-temperature-monitor", 20);
        store.storeDuration("second-service", 30);
        store.storeDuration("third-service", 40);
        assertThat(store.getDurations("second-service"))
                .isEqualTo(List.<Long>of(30l));
    }

    @Test
    public void shouldWorkWithMultiThread() {
        var result = IntStream.range(0, 50000)
                .parallel()
                .boxed()
                .allMatch(value -> {
                    store.storeDuration("body-temperature-monitor", 15l);
                    var durations = store.getDurations("body-temperature-monitor");
                    var lastDuration = durations.get(durations.size() - 1);
                    return lastDuration == 15l;
                });
        assertTrue(result);
    }

}
