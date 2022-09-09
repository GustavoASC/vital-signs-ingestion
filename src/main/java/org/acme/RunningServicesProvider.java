package org.acme;

import java.util.List;
import java.util.Map;

public interface RunningServicesProvider {

    public Map<String, List<Integer>> provideRankings();

}
