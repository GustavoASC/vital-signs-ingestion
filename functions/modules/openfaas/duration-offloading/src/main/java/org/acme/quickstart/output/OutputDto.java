package org.acme.quickstart.output;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
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
