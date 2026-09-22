package co.wethinkcode.trafficflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import co.wethinkcode.trafficflow.mq.MqConfig;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class RoutingServiceApp {

    private static volatile int latestCongestionLevel = 4;

    public static void main(String[] args) throws Exception {

        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();

        ConnectionFactory factory =
                new ActiveMQConnectionFactory(
                        MqConfig.BROKER_URL);

        Connection connection =
                factory.createConnection();

        connection.start();

        Session session =
                connection.createSession(
                        false,
                        Session.AUTO_ACKNOWLEDGE);

        Topic topic =
                session.createTopic(
                        MqConfig.TOPIC);

        MessageConsumer consumer =
                session.createConsumer(topic);

        consumer.setMessageListener(message -> {

            try {

                String json =
                        ((TextMessage) message)
                                .getText();

                Map<?, ?> update =
                        mapper.readValue(
                                json,
                                Map.class);

                Object level =
                        update.get("level");

                if (level instanceof Number) {

                    latestCongestionLevel =
                            ((Number) level).intValue();

                    System.out.println(
                            "Received congestion level: "
                                    + latestCongestionLevel);
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        });

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

                int estimatedTravelTime =
                        10 + (latestCongestionLevel * 5);

                ctx.json(
                        Map.of(
                                "intersectionId",
                                id,
                                "congestionLevel",
                                latestCongestionLevel,
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