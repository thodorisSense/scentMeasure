package eu.iccs.scent.scentmeasure.Data;

import java.util.List;

/**
 * Created by theodoropoulos on 5/10/2018.
 */

public class MeasureExplore {
    public MeasureExplore(String userID, String poiID, String msg, List<NetworkPacket>  measurements) {
        this.userID = userID;
        this.poiID = poiID;
        this.msg = msg;
        this.measurements=measurements;
    }

    public MeasureExplore(String userID, String poiID, String msg) {
        this.userID = userID;
        this.poiID = poiID;
        this.msg = msg;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPoiID() {
        return poiID;
    }

    public void setPoiID(String poiID) {
        this.poiID = poiID;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    String userID;
    String poiID;
    String msg;
    List<NetworkPacket>  measurements;

    public List<NetworkPacket>  getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<NetworkPacket>  measurements) {
        this.measurements = measurements;
    }


}
