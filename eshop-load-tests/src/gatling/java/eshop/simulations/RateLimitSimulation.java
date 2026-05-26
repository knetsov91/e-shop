package eshop.simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class RateLimitSimulation extends Simulation {

    HttpProtocolBuilder protocol = http
            .baseUrl("http://localhost");

    ScenarioBuilder scenario = scenario("Rate Limit")
            .exec(http("GET /api/v1/products")
                    .get("/api/v1/products")
                    .check(status().is(200)));

    {
        setUp(scenario.injectOpen(atOnceUsers(500)))
                .protocols(protocol)
                .assertions(
                        global().requestsPerSec().gt(100.0),
                        global().failedRequests().percent().gt(0.0),
                        global().failedRequests().percent().lt(100.0)
                );
    }
}
