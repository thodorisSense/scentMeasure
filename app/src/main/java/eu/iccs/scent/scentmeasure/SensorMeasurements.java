package eu.iccs.scent.scentmeasure;

import android.hardware.Sensor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by theodoropoulos on 26/10/2017.
 */

public class SensorMeasurements {

    SensorMeasurements(byte[] rawData){

        //Initialise humidity
        short temp;
        double sensorTempStep=0.1;
        temp=(short) (((rawData[1] & 0xFF) << 8 ) | (rawData[0] & 0xFF) );
        temperature= temp* (int)sensorTempStep;

        //Initialise humidity
        humidity= rawData[7];
        temperature= temp*sensorTempStep;

        //Set time
        Calendar rightNow = Calendar.getInstance();
        Date date = rightNow.getTime();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        timestamp=dateFormat.format(date);

    }

    int humidity;
    double  temperature;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    String timestamp;

}
