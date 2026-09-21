package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Intersection {

    private String intersectionId;
    private String district;
    private String signalType;
    private Boolean active;

    @JsonCreator
    public Intersection(
            @JsonProperty("intersectionId") String intersectionId,
            @JsonProperty("district") String district,
            @JsonProperty("signalType") String signalType,
            @JsonProperty("active") Boolean active) {

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

    public Boolean getActive() {
        return active;
    }
}