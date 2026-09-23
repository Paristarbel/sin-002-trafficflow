package co.wethinkcode.trafficflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import co.wethinkcode.trafficflow.mq.MqConfig;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoutingServiceApp {

    private static final AtomicInteger cachedCongestionLevel = new AtomicInteger(4);

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // Initialize ActiveMQ Consumer Subscription
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(MqConfig.TOPIC);
            MessageConsumer consumer = session.createConsumer(topic);

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        String text = ((TextMessage) message).getText();
                        Map<?, ?> payload = mapper.readValue(text, Map.class);
                        Object levelValue = payload.get("level");
                        if (levelValue instanceof Number) {
                            int newLevel = ((Number) levelValue).intValue();
                            if (newLevel >= 0 && newLevel <= 8) {
                                cachedCongestionLevel.set(newLevel);
                                System.out.println("Routing service cache updated to congestion level: " + newLevel);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing JMS message: " + e.getMessage());
                }
            });

            System.out.println("Successfully subscribed to ActiveMQ topic: " + MqConfig.TOPIC);

        } catch (Exception e) {
            System.err.println("Failed to initialize ActiveMQ connection: " + e.getMessage());
        }

        app.get("/route/{id}", ctx -> {
            String id = ctx.pathParam("id");

            try {
                HttpRequest intersectionRequest =
                        HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:7021/intersections/" + id))
                                .GET()
                                .build();

                HttpResponse<String> intersectionResponse =
                        client.send(intersectionRequest, HttpResponse.BodyHandlers.ofString());

                if (intersectionResponse.statusCode() == 404) {
                    ctx.status(404).result("Unknown intersection");
                    return;
                }

                if (intersectionResponse.statusCode() != 200) {
                    ctx.status(503).result("Intersection service unavailable");
                    return;
                }

                // Pulled from local ActiveMQ event-driven memory cache
                int level = cachedCongestionLevel.get();

                int baseTravelTime = 10;
                int estimatedTravelTime = baseTravelTime + (level * 5);

                ctx.json(
                        Map.of(
                                "intersectionId", id,
                                "congestionLevel", level,
                                "estimatedTravelTimeMinutes", estimatedTravelTime
                        )
                );

            } catch (Exception e) {
                ctx.status(503).result("Dependency service unavailable");
            }
        });
    }
}
