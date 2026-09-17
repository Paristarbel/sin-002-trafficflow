package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IngestionServiceApp {

    public static void main(String[] args) throws IOException {

        Javalin app = Javalin.create().start(7020);
        List<String[]> intersections = new ArrayList<>();

        app.get("/health", ctx -> ctx.result("OK"));
        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.

        app.get("/intersections",cxt -> cxt.json(intersections));

        InputStream input = IngestionServiceApp.class
                .getClassLoader()
                .getResourceAsStream("intersections-legacy.csv");

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(input));

        String line;

        reader.readLine();

        while ((line = reader.readLine()) != null) {

            String[] data = line.split(",");

            // Clean whitespace and casing
            for (int i = 0; i < data.length; i++) {
                data[i] = data[i].trim().toLowerCase();
            }
            intersections.add(data);


            for (int i = 0; i < data.length; i++) {
                System.out.println(data[i]);
            }



            String activeFlag = data[3];

            boolean active;

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

                active = false;
            }

            System.out.println("Active: " + active);
            System.out.println("----------------");
        }

        reader.close();
    }
}