package eu.iccs.scent.scentmeasure.Data;

/**
 * Created by theodoropoulos on 12/2/2018.
 */

public class Measurement {
    private String type;
    private double value;
    private String unit;
    private int accuracy;
    private long takenAt;

    public Measurement(String type, double value, String unit, int accuracy, long takenAt) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.accuracy = accuracy;
        this.takenAt = takenAt;
    }
}
