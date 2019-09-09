package eu.iccs.scent.scentmeasure.Data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by theodoropoulos on 28/6/2018.
 */
@Entity
public class UserOptions {


    public UserOptions(String timestamp, String userName, Boolean uploadMeaasurements, Boolean wifiSynch, String units) {
        this.timestamp = timestamp;
        this.userName = userName;
        this.uploadMeaasurements = uploadMeaasurements;
        this.wifiSynch = wifiSynch;
        this.units = units;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getUploadMeaasurements() {
        return uploadMeaasurements;
    }

    public void setUploadMeaasurements(Boolean uploadMeaasurements) {
        this.uploadMeaasurements = uploadMeaasurements;
    }

    public Boolean getWifiSynch() {
        return wifiSynch;
    }

    public void setWifiSynch(Boolean wifiSynch) {
        this.wifiSynch = wifiSynch;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    @PrimaryKey
    String timestamp;

    @ColumnInfo(name= "user_name")
    String userName;

    @ColumnInfo(name= "upload_measurements")
    Boolean uploadMeaasurements;

    @ColumnInfo(name= "wifi_synch")
    Boolean wifiSynch;

    @ColumnInfo(name= "unit_system")
    String units;





}
