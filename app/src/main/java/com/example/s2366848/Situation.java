
package com.example.s2366848;

// Stores information about an individual traffic situation.
public class Situation {

    private String type;
    private String comment;
    private String roadName;
    private String latitude;
    private String longitude;
    private String startTime;
    private String endTime;
    private String delay;
    private String validityStatus;
    private String impact;

    // Store all information about an individual traffic situation
    public Situation(String type, String comment, String roadName,
                     String latitude, String longitude,
                     String startTime, String endTime,
                     String delay, String validityStatus,
                     String impact) {

        this.type = type;
        this.comment = comment;
        this.roadName = roadName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startTime = startTime;
        this.endTime = endTime;
        this.delay = delay;
        this.validityStatus = validityStatus;
        this.impact = impact;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDelay() {
        return delay;
    }

    public String getValidityStatus() {
        return validityStatus;
    }

    // Return the impact information
    public String getImpact() {
        return impact;
    }
}
