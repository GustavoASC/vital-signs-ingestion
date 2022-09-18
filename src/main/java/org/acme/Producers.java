package org.acme;

import java.time.Clock;

import javax.inject.Singleton;
import javax.ws.rs.Produces;

@Singleton
public class Producers {

    @Produces
    @Singleton
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
