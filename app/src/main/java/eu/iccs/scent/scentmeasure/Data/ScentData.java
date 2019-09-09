package eu.iccs.scent.scentmeasure.Data;

import java.util.ArrayList;

/**
 * Created by theodoropoulos on 12/2/2018.
 */

public class ScentData {

    private String sensorId;
    private int datasetId;
    private Coordinates geoSensorCoord;
    private String userId;
    ArrayList<Measurement> measurementList = new ArrayList<Measurement>();
    private int trustLevel;

    public ScentData(String sensorId, int datasetId, Coordinates geoSensorCoord, String userId, ArrayList<Measurement> measurementsList, int trustLevel) {
        this.sensorId = sensorId;
        this.datasetId = datasetId;
        this.geoSensorCoord = geoSensorCoord;
        this.userId = userId;
        this.measurementList = measurementsList;
        this.trustLevel = trustLevel;
    }
}
