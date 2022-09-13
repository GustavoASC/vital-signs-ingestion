package org.acme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
public class RunningServicesProviderImpl implements RunningServicesProvider {

    private final Map<String, List<Integer>> services = new HashMap<>();

    @Override
    public void addRunningService(String service, int ranking) {
        List<Integer> rankings = services.get(service);
        if (rankings == null) {
            rankings = new ArrayList<>();
            services.put(service, rankings);
        }
        rankings.add(ranking);
    }

    @Override
    public void removeRunningService(String service, int ranking) {
        List<Integer> rankings = services.get(service);
        if (rankings != null) {
            rankings.remove(Integer.valueOf(ranking));
            if (rankings.isEmpty()) {
                services.remove(service);
            }
        }        
    }

    @Override
    public Map<String, List<Integer>> provideRankings() {
        return new HashMap<>(services);
    }

}
