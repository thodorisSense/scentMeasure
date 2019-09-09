package eu.iccs.scent.scentmeasure.Data;

import java.util.ArrayList;

/**
 * Created by theodoropoulos on 12/2/2018.
 */

public class NetworkPacket {
    ArrayList<ScentData> data;

    public NetworkPacket(ArrayList<ScentData> data) {
        this.data = data;
    }
}
