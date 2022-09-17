package org.acme;

import java.util.List;

public interface RunningServicesProvider {

    void addRunningService(String service, int ranking);
    void removeRunningService(String service, int ranking);
    List<Integer> provideAllRankings();

}
