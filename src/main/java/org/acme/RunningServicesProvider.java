package org.acme;

import java.util.List;
import java.util.Map;

public interface RunningServicesProvider {

    void addRunningService(String service, int ranking);
    void removeRunningService(String service, int ranking);
    Map<String, List<Integer>> provideRankings();

}
