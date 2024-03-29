package org.acme.quickstart;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.quickstart.input.InputDto;
import org.acme.quickstart.offloading.CouldNotDetermineException;
import org.acme.quickstart.offloading.OffloadingHeuristicByDuration;
import org.acme.quickstart.output.OutputDto;
import org.acme.quickstart.output.OutputDto.OffloadingDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class DurationOfffoadingResource {

    private final Logger logger = LoggerFactory.getLogger(DurationOfffoadingResource.class);
    private final OffloadingHeuristicByDuration offloadingHeuristic;

    public DurationOfffoadingResource(OffloadingHeuristicByDuration offloadingHeuristic) {
        this.offloadingHeuristic = offloadingHeuristic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public OutputDto function(InputDto input) {

        try {
            logger.info("Input: " + input);
 
            boolean shouldOffload = offloadingHeuristic.shouldOffloadVitalSigns(
                    input.getDurationsRunningServices(),
                    input.getDurationsTargetService());

            logger.info("Should offload: " + shouldOffload);
            return new OutputDto(shouldOffload ? OffloadingDecision.OFFLOAD : OffloadingDecision.RUN_LOCALLY);

        } catch (CouldNotDetermineException e) {
            logger.error("Unexpected error: ", e);
            return new OutputDto(OffloadingDecision.UNKNOWN);
        } finally {

            System.out.println("Invoking garbage collector...");
            System.gc();
            
        }
    }
}