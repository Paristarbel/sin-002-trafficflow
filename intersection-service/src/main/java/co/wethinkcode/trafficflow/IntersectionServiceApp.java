package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IntersectionServiceApp {

    public static void main(String[] args) throws Exception {

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7020/intersections"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Raw JSON: " + response.body());
        System.out.println("Status code: " + response.statusCode());

        System.out.println("About to parse JSON...");

        ObjectMapper mapper = new ObjectMapper();
        Intersection[] intersections;

        try {
            intersections = mapper.readValue(response.body(), Intersection[].class);
            System.out.println("Parsed count: " + intersections.length);
        } catch (Exception e) {
            System.out.println("PARSING FAILED:");
            e.printStackTrace();
            return;
        }

        List<Intersection> intersectionList = Arrays.asList(intersections);

        app.get("/intersections/{id}", ctx -> {

            String id = ctx.pathParam("id").toLowerCase();

            for (Intersection intersection : intersectionList) {
                if (intersection.getIntersectionId().equals(id)) {
                    ctx.json(intersection);
                    return;
                }
            }

            ctx.status(404).result("Intersection not found");
        });

        app.get("/districts/{district}", ctx -> {

            String district = ctx.pathParam("district").toLowerCase();

            for (Intersection intersection : intersectionList) {
                if (intersection.getDistrict() != null && intersection.getDistrict().equals(district)) {
                    ctx.json(intersection);
                    return;
                }
            }

            ctx.status(404).result("District not found");
        });
    }
}