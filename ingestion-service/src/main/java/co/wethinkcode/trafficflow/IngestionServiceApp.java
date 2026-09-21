package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.io.*;
import java.util.*;

public class IngestionServiceApp {

    public static void main(String[] args) throws IOException {

        Javalin app = Javalin.create().start(7020);

        Map<String, Intersection> intersections = new LinkedHashMap<>();

        app.get("/health", ctx -> ctx.result("OK"));

        InputStream input = IngestionServiceApp.class
                .getClassLoader()
                .getResourceAsStream("intersections-legacy.csv");

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(input));

        reader.readLine(); // skip header

        String line;

        while ((line = reader.readLine()) != null) {

            String[] data = line.split(",", -1);

            for (int i = 0; i < data.length; i++) {
                data[i] = data[i]
                        .trim()
                        .replaceAll("\\s+", " ");
            }

            String id = data[0].toUpperCase();

            String district = cleanValue(data[1]);
            if (district != null) {
                district = titleCase(district);
            }

            String signalType = cleanValue(data[2]);
            if (signalType != null) {
                signalType = signalType.toLowerCase();
            }

            Boolean active = parseBoolean(data[3]);

            Intersection intersection =
                    new Intersection(
                            id,
                            district,
                            signalType,
                            active
                    );

            intersections.putIfAbsent(id, intersection);
        }

        reader.close();

        app.get("/intersections",
                ctx -> ctx.json(intersections.values()));
    }

    private static Boolean parseBoolean(String value) {

        if (value == null) return null;

        value = value.trim().toLowerCase();

        switch (value) {
            case "y":
            case "yes":
            case "true":
            case "1":
                return true;

            case "n":
            case "no":
            case "false":
            case "0":
                return false;

            default:
                return null;
        }
    }

    private static String cleanValue(String value) {

        if (value == null) return null;

        value = value.trim();

        if (value.isEmpty()
                || value.equalsIgnoreCase("n/a")
                || value.equalsIgnoreCase("tbd")
                || value.equalsIgnoreCase("unknown")
                || value.equalsIgnoreCase("nan")
                || value.equals("-")) {
            return null;
        }

        return value;
    }

    private static String titleCase(String text) {

        String[] words = text.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {

            if (word.isEmpty()) continue;

            result.append(
                            Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return result.toString().trim();
    }
}