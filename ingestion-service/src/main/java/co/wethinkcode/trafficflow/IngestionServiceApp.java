package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.util.*;
import java.io.*;

public class IngestionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7020);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.

        BufferedReader reader = new BufferedReader("src/main/resources/intersections-legacy.csv");

        String line;

        while((line = reader.readLine()) != null ){

            String[] data = line.split(",");

            for(int i =0  ; i< data.length ; i++){
                System.out.println(data[i]);
            }

        }
        reader.close();


    }

}
