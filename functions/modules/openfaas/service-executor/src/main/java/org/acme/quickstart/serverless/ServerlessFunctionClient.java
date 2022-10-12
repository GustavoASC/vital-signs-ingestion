package org.acme.quickstart.serverless;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.acme.quickstart.offloading.duration.OffloadDurationInputDto;
import org.acme.quickstart.offloading.duration.OffloadDurationOutputDto;
import org.acme.quickstart.offloading.ranking.OffloadRankingInputDto;
import org.acme.quickstart.offloading.ranking.OffloadRankingOutputDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "serverless-function")
@Path("/function/{fn_name}")
@ApplicationScoped
public interface ServerlessFunctionClient {

    @POST
    String runFunction(@PathParam("fn_name") String fnName, String payload);

    @POST
    OffloadRankingOutputDto runRankingOffloading(@PathParam("fn_name") String fnName, OffloadRankingInputDto payload);

    @POST
    OffloadDurationOutputDto runOffloadingDuration(@PathParam("fn_name") String fnName, OffloadDurationInputDto payload);

}
