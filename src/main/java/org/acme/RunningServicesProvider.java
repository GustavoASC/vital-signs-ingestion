package org.acme;

import java.util.List;

public interface RunningServicesProvider {

    void executionStarted(String service, int ranking);
    void executionFinished(String service, int ranking);
    List<Integer> getRankingsForRunningSerices();

}
