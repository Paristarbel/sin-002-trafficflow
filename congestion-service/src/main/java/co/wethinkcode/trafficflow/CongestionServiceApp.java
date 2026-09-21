package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CongestionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7022);


        // TODO (Tracks the city-wide Congestion Level (0-8).)
        // Add domain endpoints for congestion-service here.
        AtomicInteger congestionLevel =
                new AtomicInteger(4);
       app.get("/health",
                ctx -> ctx.result("OK"));

        app.get("/congestion",
                ctx -> ctx.json(
                        Map.of(
                                "level",
                                congestionLevel.get()
                        )
                ));

        app.post("/congestion/{level}", ctx -> {

            int level =
                    Integer.parseInt(
                            ctx.pathParam("level"));
           if (level < 0 || level > 8) {

                ctx.status(400)
                        .result("Level must be between 0 and 8");

                return;
            }

            congestionLevel.set(level);

            ctx.json(
                    Map.of(
                            "level",
                            congestionLevel.get()
                    )
            );
        });
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)