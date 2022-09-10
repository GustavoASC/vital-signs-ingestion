package org.acme;

import java.util.List;

public interface VitalSignService {

    void ingestVitalSign(List<String> services, String vitalSign, int userPriority);
    void ingestVitalSignRunningAllServices(String vitalSign, int userPriority);

}
