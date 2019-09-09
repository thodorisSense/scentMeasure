package eu.iccs.scent.scentmeasure;

import android.bluetooth.BluetoothDevice;

/**
 * Created by theodoropoulos on 23/10/2017.
 */

public class BleDevice {

    BleDevice(BluetoothDevice device, String deviceid, String info, String alias){
        bleDeviceId=deviceid;
        bleDeviceInfo=info;
        this.device=device;
        this.bleDeviceAlias=alias;
        connect=false;
    }
    String bleDeviceId;
    String bleDeviceInfo;
    BluetoothDevice device;
    Boolean connect;
    String bleDeviceAlias;
}
