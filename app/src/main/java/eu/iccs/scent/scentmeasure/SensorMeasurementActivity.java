package eu.iccs.scent.scentmeasure;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.jar.Manifest;

import eu.iccs.scent.scentmeasure.Data.AppDatabase;
import eu.iccs.scent.scentmeasure.Data.Coordinates;
import eu.iccs.scent.scentmeasure.Data.MeasureExplore;
import eu.iccs.scent.scentmeasure.Data.Measurement;
import eu.iccs.scent.scentmeasure.Data.NetworkPacket;
import eu.iccs.scent.scentmeasure.Data.ScentData;
import eu.iccs.scent.scentmeasure.Data.UserOptions;
import eu.iccs.scent.scentmeasure.Data.UserOptionsDao;
import eu.iccs.scent.scentmeasure.Network.Connectivity;

import static android.R.id.list;


public class SensorMeasurementActivity extends AppCompatActivity implements LocationListener {
    public String mDeviceAddress;
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private BluetoothLeService mBluetoothLeService;
    int requestId;
    LocationManager locationManager;
    Intent gattServiceIntent;
    ViewPager viewPager;
    Location lastKnownLocation;
    String mUserName;
    String mUserEmail;
    Boolean mLoggedIn;
    static SensorMeasurementActivity sensorMeasurementActivity;
    public boolean measurementsCollected;
    public int measurementsAll=0;

    public static final String PREFS_NAME = "MinMaxFile";
    public static final String MIN_TEMP = "minTemp";
    public static final String MAX_TEMP = "maxTemp";
    public static final String MIN_MOIST = "minMoist";
    public static final String MAX_MOIST = "maxMoist";
    public static final String LAST_MEASUREMENTS = "last_measurement";
    public static final String LAST_POSITION = "last_position";
    public static final String LAST_LOCATION = "last_location";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String LOGGEDIN = "loggedin";
    public static final String POIID = "poiid";
    public static final String USERID="userid";
    public static final String USERSCORE="score";
    public static final short MEASUREUPDATECOUNT = 4;


    AppDatabase db;
    UserOptionsDao userOptionsDao;
    UserOptions userOptions;
    public short measurementCount=0;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e( "Error", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public static SensorMeasurementActivity getInstance(){
        return   sensorMeasurementActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorMeasurementActivity=this;
        measurementsCollected=false;
        setContentView(R.layout.activity_sensor_measurement);
        this.measurementCount=0;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if (checkPermission(Manifest))
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        }
        catch (SecurityException e){

        }
        //Get intent and BLE device address.
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //Get usernmame
        SharedPreferences sharedPreferences = getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
        mUserName=sharedPreferences.getString(USERNAME,"");

        //Get email
        mUserEmail=sharedPreferences.getString(EMAIL,"");

        //Get loggedin status
        mLoggedIn=sharedPreferences.getBoolean(LOGGEDIN,false);

        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        //Here check the username and if it does not exist
                        db = AppDatabase.getDatabase(getApplicationContext());
                        userOptionsDao = db.userOptionsDao();
                        userOptions = userOptionsDao.findByName(mUserName);

                        if (userOptions == null) {

                            DateFormat df = DateFormat.getTimeInstance();
                            df.setTimeZone(TimeZone.getTimeZone("gmt"));
                            String gmtTime = df.format(new Date());
                            boolean measurementsChecked = true;
                            userOptions = new UserOptions(gmtTime, mUserName, measurementsChecked, false, "Metric");
                            userOptionsDao.insertAll(userOptions);

                        }
                    }
                });
        thread.start();
        try {
            thread.join();
        }catch (Exception e){

        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SensorFragmentPagerAdapter(getSupportFragmentManager(),
                SensorMeasurementActivity.this));

            // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);


        //Register the bluetooth service
        //gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Run this when on resume happens
        //Toast.makeText(this,"IN RESUME",Toast.LENGTH_LONG ).show();

        //  registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
      //      bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (measurementsCollected==true)
            unbindService(mServiceConnection);
        locationManager.removeUpdates(this);
        if (measurementsCollected==true)
            unregisterReceiver(mGattUpdateReceiver);
    }

    //Start measuring is invoked from the
    //interface of the
    protected void startMeasuring(){

        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);

        this.measurementsCollected=true;

        // based on the current position you can then cast the page to the correct Fragment class and call some method inside that fragment to reload the data:
        if (0==viewPager.getCurrentItem() && null != page) {
            ((TemperatureFragment)page).setSensorCollectingMeasurements(true);
        }

        page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
        if (1==viewPager.getCurrentItem() && null != page) {
            ((MoistureFragment)page).setSensorCollectingMeasurements(true);
        }


    }

    protected void stopMeasuring(){
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
        // based on the current position you can then cast the page to the correct Fragment class and call some method inside that fragment to reload the data:
        this.measurementsCollected=false;
        if (0==viewPager.getCurrentItem() && null != page) {
            ((TemperatureFragment)page).setSensorCollectingMeasurements(false);
        }

        page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
        if (1==viewPager.getCurrentItem() && null != page) {
            ((MoistureFragment)page).setSensorCollectingMeasurements(false);
        }

        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
    }




    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            final Intent finalIntent=intent;
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                Log.w("In broadcast receiver", "Connected ");

                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // mConnected = false;
                //updateConnectionState(R.string.disconnected);
                Log.w("In broadcast receiver", "Disconnected ");
                //Reconnect once you are done with the connection
                //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                unbindService(mServiceConnection);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                invalidateOptionsMenu();
                //  clearUI();
            } else if (BluetoothLeService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                //  displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.w("In broadcast receiver", "Discovered ");

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //  displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));


                Log.w("In broadcast receiver", "Data available ");
                String url = "http://my-json-feed";

                //Save the latest measurement along with the timestamp in the persistence layer
                Date currentTime = Calendar.getInstance().getTime();
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                String time = df.format(currentTime);
                SensorMeasurements sensorMeasurements=new SensorMeasurements(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
                sensorMeasurements.setTimestamp(time);
                Gson gson=new Gson();
                String lastMeasurement=gson.toJson(sensorMeasurements);
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(LAST_MEASUREMENTS,lastMeasurement);
                editor.commit();
                SharedPreferences minMaxValues = getSharedPreferences(PREFS_NAME, 0);
                DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
                String dateStr = date.format(currentTime);
                String minimumMoist = (minMaxValues.getString(MIN_MOIST + dateStr, "10000.0"));
                String maximumMoist = (minMaxValues.getString(MAX_MOIST + dateStr, "-10000.0"));
                String minimumTemp = (minMaxValues.getString(MIN_TEMP + dateStr, "10000.0"));
                String maximumTemp = (minMaxValues.getString(MAX_TEMP + dateStr, "-10000.0"));

                if (Double.valueOf(minimumMoist)==10000.0){
                    editor.putString(MIN_MOIST + dateStr, Double.toString(sensorMeasurements.humidity));
                    editor.commit();
                }
                else if (( Double.valueOf(minimumMoist) > sensorMeasurements.humidity)) {

                    editor.putString(MIN_MOIST + dateStr, Double.toString(sensorMeasurements.humidity));
                    editor.commit();

                }

                if (Double.valueOf(minimumMoist)==10000.0){
                    editor.putString(MAX_MOIST + dateStr, Double.toString(sensorMeasurements.humidity));
                    editor.commit();
                }
                else if (( Double.valueOf(maximumMoist) < sensorMeasurements.humidity)) {

                    editor.putString(MAX_MOIST + dateStr, Double.toString(sensorMeasurements.humidity));
                    editor.commit();

                }

                if (Double.valueOf(minimumTemp)==10000.0){
                    editor.putString(MIN_TEMP + dateStr, Double.toString(sensorMeasurements.temperature));
                    editor.commit();
                }
                else if (( Double.valueOf(minimumTemp) > sensorMeasurements.temperature)) {

                    editor.putString(MIN_TEMP + dateStr, Double.toString(sensorMeasurements.temperature));
                    editor.commit();

                }

                if (Double.valueOf(minimumTemp)==-10000.0){
                    editor.putString(MAX_TEMP + dateStr, Double.toString(sensorMeasurements.temperature));
                    editor.commit();
                }
                else if (( Double.valueOf(maximumTemp) < sensorMeasurements.temperature)) {

                    editor.putString(MAX_TEMP + dateStr, Double.toString(sensorMeasurements.temperature));
                    editor.commit();

                }


                Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
                // based on the current position you can then cast the page to the correct Fragment class and call some method inside that fragment to reload the data:
                if (0==viewPager.getCurrentItem() && null != page) {
                    ((TemperatureFragment)page).setSensorUI(sensorMeasurements);

                }

                page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
                if (1==viewPager.getCurrentItem() && null != page) {
                    ((MoistureFragment)page).setSensorUI(sensorMeasurements);

                }

                //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                //TODO: Implement communication with the backend
                //If data upload is enabled for the user
                //
                //If data upload is enabled for the user


                //Set vies according to options
                Thread thread = new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                //Here check the username and if it does not exist
                                db = AppDatabase.getDatabase(getApplicationContext());
                                userOptionsDao = db.userOptionsDao();
                                userOptions = userOptionsDao.findByName(mUserName);
                                //Check the network status
                                Connectivity connectivity=new Connectivity();
                                if ((userOptions.getUploadMeaasurements()==true))
                                {
                                    sendDataToBackend(finalIntent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA), requestId, userOptions.getWifiSynch());
                                }
                            }
                        });
                thread.start();
                try {
                }catch (Exception e){

                }


              //  sendDataToBackend(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA),requestId);
              //  requestId++;

            }
        }
    };

    private void evaluateMeasurementCount()
    {
        SharedPreferences loginSharedPreferences= getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
        SharedPreferences.Editor editor=loginSharedPreferences.edit();
        SharedPreferences sensorActivitySensorPreferences=getSharedPreferences(SensorMeasurementActivity.PREFS_NAME,0);

        if (appMode.equals("explore"))
        {
            int measurementCount=loginSharedPreferences.getInt(LoginActivity.MEASUREMENTCOUNT, 0);
            measurementCount++;
            //Toast.makeText(this,"Measurement count is"+ String.valueOf(measurementCount),Toast.LENGTH_SHORT).show();
            editor.putInt(LoginActivity.MEASUREMENTCOUNT,measurementCount);
            editor.commit();
            if (measurementCount==SensorMeasurementActivity.MEASUREUPDATECOUNT)
            {
                //Open the scentExplore app
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.xteamsoftware.scentexplore");
                //Data of the class here
                MeasureExplore data=new MeasureExplore(sensorActivitySensorPreferences.getString(SensorMeasurementActivity.USERNAME,""),
                        sensorActivitySensorPreferences.getString(SensorMeasurementActivity.POIID,""), "complete");
                Gson gson=new Gson();
                String dataMeasureExplore=gson.toJson( data);
                Log.d("Message",dataMeasureExplore);
                launchIntent.putExtra("arguments", dataMeasureExplore );
                editor.putInt(LoginActivity.MEASUREMENTCOUNT,0);
                editor.putString(LoginActivity.APPMODE,"standalone");
                editor.commit();
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(launchIntent);//null pointer check in case package name was not found
                finish();
            }

        }
    }

    private void evaluateMeasurementCountAndBuffer(String jsonBody)
    {
        SharedPreferences loginSharedPreferences= getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor=loginSharedPreferences.edit();
        SharedPreferences sensorActivitySensorPreferences=getSharedPreferences(SensorMeasurementActivity.PREFS_NAME,0);

        int measurementCount=loginSharedPreferences.getInt(LoginActivity.MEASUREMENTCOUNT, 0);
        measurementCount++;
        //Toast.makeText(this,"Measurement count is"+ String.valueOf(measurementCount),Toast.LENGTH_SHORT).show();
        editor.putInt(LoginActivity.MEASUREMENTCOUNT,measurementCount);
        editor.commit();

        //Here update the status of the indicator when in explore mode
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
        // based on the current position you can then cast the page to the correct Fragment class and call some method inside that fragment to reload the data:
        if (0==viewPager.getCurrentItem() && null != page) {
            ((TemperatureFragment)page).updateStatusBar(measurementCount);

        }

        page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
        if (1==viewPager.getCurrentItem() && null != page) {
            ((MoistureFragment)page).updateStatusBar(measurementCount);

        }

        ArrayList<String> list =new ArrayList<>();
        list = getStringArrayPref(this, "scentDataExplore");
        list.add(jsonBody);
        setStringArrayPref(getApplicationContext(), "scentDataExplore", list);
        SensorMeasurementActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.storedData), Toast.LENGTH_SHORT).show();
            }
        });
        Gson gson=new Gson();
        //Here get the data from the list and
        if (measurementCount==SensorMeasurementActivity.MEASUREUPDATECOUNT)
        {
            //Open the scentExplore app
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.xteamsoftware.scentexplore");
            //Data of the class here
            list = getStringArrayPref(this, "scentDataExplore");
            List<NetworkPacket> networkPackets=new ArrayList<>();

            for (int i=0;i<list.size();i++){
                NetworkPacket netwowrkPacket=(NetworkPacket) gson.fromJson(list.get(i), NetworkPacket.class);
                networkPackets.add(netwowrkPacket);
            }

            list.clear();
            setStringArrayPref(getApplicationContext(), "scentDataExplore", list);
            MeasureExplore data=new MeasureExplore( Integer.toString(sensorActivitySensorPreferences.getInt(SensorMeasurementActivity.USERID,0)),
                    sensorActivitySensorPreferences.getString(SensorMeasurementActivity.POIID,""), "complete", networkPackets);
            String dataMeasureExplore=gson.toJson(data);
            Log.d("Message",dataMeasureExplore);
            launchIntent.putExtra("arguments", dataMeasureExplore );
            editor.putInt(LoginActivity.MEASUREMENTCOUNT,0);
            editor.putString(LoginActivity.APPMODE,"standalone");
            editor.commit();
           // launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           // launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launchIntent);//null pointer check in case package name was not found
            finish();
            }
    }

    private void sendDataToBackend(byte[] bytes, int id, boolean wifiSynch){

        String url = "http://scent.u-hopper.com/api/sensorData";
        //Create JSON object from data

        SensorMeasurements sensorMeasurements=new SensorMeasurements(bytes);
        //Create SCENT network data class
        //TODO: SensorId must be a string. It;s value will be the mDeviceAddress variable
        //TODO: DatasetId will be updated from the backend it should be removed from the api
        //TODO: We have to find out what the user id is
        try {
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            Date date = (Date)dateFormat.parse(sensorMeasurements.timestamp);
            Measurement measurementTemp=new Measurement("temperature",   sensorMeasurements.temperature, "celcius", 1, date.getTime());
            Measurement measurementMoist=new Measurement("moisture", sensorMeasurements.humidity, "percentage", 1, date.getTime());
            ArrayList<Measurement> listOfMeasurements=new ArrayList<>();
            listOfMeasurements.add(measurementTemp);
            listOfMeasurements.add(measurementMoist);
            Coordinates coordinates;
            if (lastKnownLocation==null){
                coordinates=  new Coordinates(0.0 ,0.0,
                        0.0,0);
                return;

            }
            else{
                coordinates=  new Coordinates(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),
                        lastKnownLocation.getAltitude(),lastKnownLocation.getAccuracy());
            }
            SharedPreferences userAttrs = getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
            int userName =userAttrs.getInt(SensorMeasurementActivity.USERID, 0);
            //Here get the bluetooth identifies and input it

            ScentData scentData=new ScentData(mDeviceAddress,0, coordinates, Integer.toString(userName),listOfMeasurements, 0 );
            ArrayList<ScentData> data=new ArrayList<>();
            data.add(scentData);
            NetworkPacket dataNet=new NetworkPacket(data);
            Gson mGson = new Gson();
            final String jsonBody = mGson.toJson(dataNet);
            Log.v("Json is", jsonBody);
            JSONObject jsonObject = null;
            Log.v("Sent message", "Sending network request");
            SharedPreferences loginSharedPreferences= getSharedPreferences(LoginActivity.PREFS_NAME, 0);
            String appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
            if (appMode.equals("explore"))
            {
                evaluateMeasurementCountAndBuffer(jsonBody);

            }else
            {
                measurementsAll++;

                Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
                if (0==viewPager.getCurrentItem() && null != page) {
                    ((TemperatureFragment)page).updateStatusBar(measurementsAll % 5);

                }

                page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
                if (1==viewPager.getCurrentItem() && null != page) {
                    ((MoistureFragment)page).updateStatusBar(measurementsAll % 5);

                }
            //If we are in explore mode then evaluate measurement count and
            //pass data to the scent  explore app.
            //Send all requests
            Connectivity connectivity=new Connectivity();
            if ( ((isNetworkAvailable()) && (connectivity.isConnectedWifi(getApplicationContext())) && (wifiSynch))
                   ||((isNetworkAvailable())
                    )) {

                ArrayList<String> list =new ArrayList<>();
                list = getStringArrayPref(this, "scentData");
                list.add(jsonBody);
                setStringArrayPref(getApplicationContext(), "scentData", list);
                int listSize=list.size();
                for (int listCount=0;listCount<listSize;listCount++){

                try{
                    jsonObject=new JSONObject(list.get(listCount));
                    setStringArrayPref(getApplicationContext(), "scentData", list);
                } catch (Exception e ){
                    Log.v( "Exception", e.toString());
                }

                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.v("Sent message", "Network");
                                SensorMeasurementActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText( getApplicationContext(), getString(R.string.uploadedData),Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //evaluateMeasurementCount();

                            }
                        }
                        , new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stu
                                Log.v("In error", error.toString());
                                //Add message to persistence and send it later.
                                //add string to persistens

                            }
                        })
                {@Override
                public Map< String, String > getHeaders() throws AuthFailureError {
                    HashMap < String, String > headers = new HashMap< String, String >();
                    //String encodedCredentials = Base64.encodeToString("passwordandlogin".getBytes(), Base64.NO_WRAP);
                    headers.put("Authorization", "Bearer " + "b41196a9-98ba-48a1-9435-623028157909 ");
                    //headers.put("Authorization", "Basic " + encodedCredentials);

                    return headers;
                }
                };

                // Access the RequestQueue through your singleton class.
                NetSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
                }
                //Set list to null and save again in persistence
                list.clear();
                setStringArrayPref(getApplicationContext(), "scentData", list);
            }else {

                ArrayList<String> list = getStringArrayPref(this, "scentData");
                list.add(jsonBody);
                setStringArrayPref(getApplicationContext(), "scentData", list);
                SensorMeasurementActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText( getApplicationContext(), getString(R.string.storedData),Toast.LENGTH_SHORT).show();
                    }
                });
                //evaluateMeasurementCount();

            }
        }
        }catch (Exception e){
            Log.v( "Exception", e.toString());

        }


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

        //Save location and position and upload it whenever a new fragment pops up

        double latitude = (double) (location.getLatitude());
        double longitude = (double) (location.getLongitude());
        lastKnownLocation=location;

        Gson gson=new Gson();
        Coordinates coordinates=  new Coordinates(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),
                lastKnownLocation.getAltitude(),lastKnownLocation.getAccuracy());

        String lastKnownPosition=gson.toJson(coordinates);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_POSITION,lastKnownPosition);
        editor.commit();

        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
        // based on the current position you can then cast the page to the correct Fragment class and call some method inside that fragment to reload the data:
        if (0==viewPager.getCurrentItem() && null != page) {
            ((TemperatureFragment)page).updatePosition(longitude,latitude);
        }

        page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
        if (1==viewPager.getCurrentItem() && null != page) {
            ((MoistureFragment)page).updatePosition(longitude, latitude);
        }


        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                // do your stuff

                gson=new Gson();
                String lastKnownLocationADR=gson.toJson(addresses.get(0).getLocality());
                sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
                editor = sharedPreferences.edit();
                editor.putString(LAST_LOCATION,lastKnownLocationADR);
                editor.commit();

                page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
                if (0==viewPager.getCurrentItem() && null != page) {
                    ((TemperatureFragment)page).updateLocation(addresses.get(0).getLocality());
                }

                page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);
                if (1==viewPager.getCurrentItem() && null != page) {
                    ((MoistureFragment)page).updateLocation(addresses.get(0).getLocality());
                }
            }
            else {

            }
        }catch(Exception e){

        }



    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}