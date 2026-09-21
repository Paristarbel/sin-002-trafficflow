package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class RoutingServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Provides estimated travel times based on congestion and intersection.)
        // Add domain endpoints for routing-service here.
        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();

        app.get("/route/{id}", ctx -> {

            String id = ctx.pathParam("id");

            try {

                HttpRequest intersectionRequest =
                        HttpRequest.newBuilder()
                                .uri(
                                        URI.create(
                                                "http://localhost:7021/intersections/" + id))
                                .GET()
                                .build();

                HttpResponse<String> intersectionResponse =
                        client.send(
                                intersectionRequest,
                                HttpResponse.BodyHandlers.ofString());

                if (intersectionResponse.statusCode() == 404) {
                    ctx.status(404)
                            .result("Unknown intersection");
                    return;
                }

                if (intersectionResponse.statusCode() != 200) {
                    ctx.status(503)
                            .result("Intersection service unavailable");
                    return;
                }

                HttpRequest congestionRequest =
                        HttpRequest.newBuilder()
                                .uri(
                                        URI.create(
                                                "http://localhost:7022/congestion"))
                                .GET()
                                .build();

                HttpResponse<String> congestionResponse =
                        client.send(
                                congestionRequest,
                                HttpResponse.BodyHandlers.ofString());

                if (congestionResponse.statusCode() != 200) {
                    ctx.status(503)
                            .result("Congestion service unavailable");
                    return;
                }

                Map<?, ?> congestion =
                        mapper.readValue(
                                congestionResponse.body(),
                                Map.class);

                Object levelValue = congestion.get("level");

                if (!(levelValue instanceof Number)) {
                    ctx.status(503)
                            .result("Invalid congestion response");
                    return;
                }

                int level = ((Number) levelValue).intValue();

                if (level < 0 || level > 8) {
                    ctx.status(503)
                            .result("Invalid congestion level");
                    return;
                }

                int baseTravelTime = 10;

                int estimatedTravelTime =
                        baseTravelTime + (level * 5);

                ctx.json(
                        Map.of(
                                "intersectionId", id,
                                "congestionLevel", level,
                                "estimatedTravelTimeMinutes",
                                estimatedTravelTime
                        )
                );

            } catch (Exception e) {
                ctx.status(503)
                        .result("Dependency service unavailable");
            }
        });
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)