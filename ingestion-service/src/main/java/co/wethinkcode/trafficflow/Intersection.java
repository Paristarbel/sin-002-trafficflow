package co.wethinkcode.trafficflow;

import java.util.ArrayList;
import java.util.List;

public class Intersection {

    private String intersectionId;
    private String district;
    private String signalType;
    private boolean active;

    public Intersection(String intersectionId,
                        String district,
                        String signalType,
                        boolean active) {

        this.intersectionId = intersectionId;
        this.district = district;
        this.signalType = signalType;
        this.active = active;

    }

    public String getIntersectionId() {
        return intersectionId;
    }

    public String getDistrict() {
        return district;
    }

    public String getSignalType() {
        return signalType;
    }

    public boolean isActive() {
        return active;
    }
}