package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class IntersectionServiceApp {

    public static void main(String[] args) throws Exception {

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7020/intersections"))
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();

        Intersection[] intersections =
                mapper.readValue(
                        response.body(),
                        Intersection[].class);

        List<Intersection> data =
                Arrays.asList(intersections);

        app.get("/intersections/{id}", ctx -> {

            String id = ctx.pathParam("id");

            for (Intersection intersection : data) {

                if (intersection.getIntersectionId()
                        .equalsIgnoreCase(id)) {

                    ctx.json(intersection);
                    return;
                }
            }

            ctx.status(404)
                    .result("Intersection not found");
        });

        app.get("/districts/{district}", ctx -> {

            String district =
                    ctx.pathParam("district");

            for (Intersection intersection : data) {

                if (intersection.getDistrict() != null
                        && intersection.getDistrict()
                        .equalsIgnoreCase(district)) {

                    ctx.json(intersection);
                    return;
                }
            }

            ctx.status(404)
                    .result("District not found");
        });
    }
}