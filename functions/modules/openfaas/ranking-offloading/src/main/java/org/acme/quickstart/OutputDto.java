package org.acme.quickstart;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@RegisterForReflection
public class OutputDto {

    private final OffloadingDecision offloadingDecision;

    public OutputDto(OffloadingDecision offloadingDecision) {
        this.offloadingDecision = offloadingDecision;
    }

    public OffloadingDecision getOffloadingDecision() {
        return this.offloadingDecision;
    }

    public static enum OffloadingDecision {

        OFFLOAD,
        RUN_LOCALLY,
        UNKNOWN 

    }

}
