package org.acme.quickstart;

import java.util.List;
import java.util.UUID;

public interface VitalSignService {

    void ingestVitalSign(UUID id, List<String> services, String vitalSign, int userPriority);
    void ingestVitalSignRunningAllServices(UUID id, String vitalSign, int userPriority);

}
