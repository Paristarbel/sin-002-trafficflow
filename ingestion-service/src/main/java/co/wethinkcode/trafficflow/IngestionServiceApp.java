package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.io.*;
import java.util.*;

public class IngestionServiceApp {

    public static void main(String[] args) throws IOException {

        Javalin app = Javalin.create().start(7020);
        Map<String, Intersection> intersections = new LinkedHashMap<>();

        app.get("/health", ctx -> ctx.result("OK"));
        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.

        app.get("/intersections", ctx -> ctx.json(intersections.values()));

        InputStream input = IngestionServiceApp.class
                .getClassLoader()
                .getResourceAsStream("intersections-legacy.csv");

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(input));

        String line;

        reader.readLine();

        while ((line = reader.readLine()) != null) {

            String[] data = line.split(",", -1);

            for (int i = 0; i < data.length; i++) {
                data[i] = data[i].trim().toLowerCase().replaceAll("\\s+", " ");
            }

            String activeFlag = data[3];

            Boolean active;

            if (activeFlag.equals("y")
                    || activeFlag.equals("yes")
                    || activeFlag.equals("true")
                    || activeFlag.equals("1")) {

                active = true;

            } else if (activeFlag.equals("no")
                    || activeFlag.equals("n")
                    || activeFlag.equals("false")
                    || activeFlag.equals("0")) {

                active = false;

            } else {

                active = null;
            }

            Intersection intersection = new Intersection(
                    data[0],
                    cleanValue(data[1]),
                    cleanValue(data[2]),
                    active);

            intersections.putIfAbsent(data[0], intersection);
        }

        reader.close();
    }

    public static String cleanValue(String value) {

        if (value.equals("")
                || value.equals("tbd")
                || value.equals("-")
                || value.equals("n/a")
                || value.equals("unknown")
                || value.equals("nan")) {
            return null;
        } else {
            return value;
        }
    }
}