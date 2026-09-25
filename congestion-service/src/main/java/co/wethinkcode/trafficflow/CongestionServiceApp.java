package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CongestionServiceApp {

    public static void main(String[] args) throws Exception {
        Javalin app = Javalin.create().start(7022);

        ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(MqConfig.TOPIC);
        MessageProducer producer = session.createProducer(topic);

        AtomicInteger congestionLevel = new AtomicInteger(4);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/congestion", ctx -> ctx.json(Map.of("level", congestionLevel.get())));

        app.post("/congestion/{level}", ctx -> {
            int level;
            try {
                level = Integer.parseInt(ctx.pathParam("level"));
            } catch (NumberFormatException e) {
                ctx.status(400).result("Level must be numeric");
                return;
            }
            if (level < 0 || level > 8) {
                ctx.status(400).result("Level must be between 0 and 8");
                return;
            }
            congestionLevel.set(level);
            String json = "{\"level\":" + level + "}";
            TextMessage message = session.createTextMessage(json);
            producer.send(message);
            System.out.println("Published congestion level: " + level);
            ctx.json(Map.of("level", level, "published", true));
        });
    }
}
