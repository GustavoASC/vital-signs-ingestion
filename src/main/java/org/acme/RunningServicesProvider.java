package org.acme;

import java.util.List;
import java.util.Map;

public interface RunningServicesProvider {

    public void addRunningService(String service, int ranking);
    public Map<String, List<Integer>> provideRankings();

}
