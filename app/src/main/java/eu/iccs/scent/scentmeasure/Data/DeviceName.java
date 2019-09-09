package eu.iccs.scent.scentmeasure.Data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by theodoropoulos on 14/6/2018.
 */
@Entity
public class DeviceName {

    public DeviceName(String timestamp, String userName, String deviceID, String deviceAlias) {
        this.timestamp=timestamp;
        this.userName = userName;
        this.deviceID = deviceID;
        this.deviceAlias = deviceAlias;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @PrimaryKey
    String timestamp;

    @ColumnInfo(name= "user_name")
    String userName;

    @ColumnInfo(name = "device_id")
    String deviceID;

    @ColumnInfo(name = "device_alias")
    String deviceAlias;

}
