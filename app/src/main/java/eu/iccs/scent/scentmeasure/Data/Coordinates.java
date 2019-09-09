package eu.iccs.scent.scentmeasure.Data;

/**
 * Created by theodoropoulos on 12/2/2018.
 */

public class Coordinates {
    private double latitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private double longitude;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String location;
    private double elevation;
    private float accuracy;

    public Coordinates(double latitude, double longitude, double elevation, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.accuracy = accuracy;
    }
}
