package eu.iccs.scent.scentmeasure;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import eu.iccs.scent.scentmeasure.Data.AppDatabase;
import eu.iccs.scent.scentmeasure.Data.DeviceAliasDao;
import eu.iccs.scent.scentmeasure.Data.DeviceName;

import static java.lang.Thread.sleep;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */


public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private ListView mListView;
    static FullscreenActivity fullScreenActivity;

    private static final boolean AUTO_HIDE = true;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    IntentFilter filter;
    public static final int MULTIPLE_PERMISSIONS = 100;

    //private LeDeviceListAdapter mLeDeviceListAdapter;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private final static String TAG = FullscreenActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private String mDeviceName;
    public String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    AppDatabase db;
    DeviceAliasDao deviceAliasDao;
    String mUserName;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            //Automatically connects to the device upon successful start-up initialization.
    //        while (true) {
               mBluetoothLeService.connect(mDeviceAddress);
   //             try {
  //                  Thread.sleep(10000);
 //               } catch (Exception e) {


                }
   //        }
  //      }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.v("Disconection event", "Disconecting from serice");
        }
    };


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(true);
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            //hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    ArrayList<BleDevice> list;
    BleDeviceAdapter adapter;
    BluetoothLeService service;

    // function to check permissions
    private void checkPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH)

                != PackageManager.PERMISSION_GRANTED ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_FINE_LOCATION)||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.BLUETOOTH)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                            new String[]{Manifest.permission
                                    .ACCESS_COARSE_LOCATION,Manifest.permission
                                    .ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH},
                            MULTIPLE_PERMISSIONS);
                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                            new String[]{Manifest.permission
                                    .ACCESS_COARSE_LOCATION,Manifest.permission
                                    .ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH},
                            MULTIPLE_PERMISSIONS);
                }
            }
        } else {
            // put your function here
            //Permissions granted go on with initialisation
            //Here check the username and if it does not exist
            mListView = (ListView) findViewById(R.id.bleDevicesListView);
            ActionBar actionBar=getSupportActionBar();
            actionBar.setTitle(getString( R.string.actionBar));
            list = new ArrayList<BleDevice>();

            adapter = new BleDeviceAdapter(getBaseContext(), list);
            mListView.setAdapter(adapter);

            int REQUEST_ENABLE_BT = 1;

            // Trigger the initial hide() shortly after the activity has been
            // created, to briefly hint to the user that UI controls
            // are available.
            delayedHide(10000);

            db = AppDatabase.getDatabase(getApplicationContext());
            deviceAliasDao = db.deviceAliasDao();

            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }


            SharedPreferences userAttrs = getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
            mUserName=userAttrs.getString(SensorMeasurementActivity.USERNAME, "unknown");
            scanLeDevice(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenActivity=this;
        setContentView(R.layout.activity_fullscreen);
        mVisible = true;
        //Create list view

        mListView = (ListView) findViewById(R.id.bleDevicesListView);
// 1
        list = new ArrayList<BleDevice>();

        adapter = new BleDeviceAdapter(getBaseContext(), list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Go to new activity somewhere here
                //Get the details of the selected device and pass it over to the
                //UI control fragment in order to retrieve sensor data
                mDeviceAddress=list.get(position).bleDeviceInfo;
                Intent intent = new Intent( getBaseContext() , SensorMeasurementActivity.class);
                intent.putExtra(SensorMeasurementActivity.EXTRAS_DEVICE_ADDRESS,mDeviceAddress);
                startActivity(intent);

                // From here on we go on with the bluetooth code.
                list.get(position).connect=true;

            }
        });

    }

    public static FullscreenActivity getInstance(){
        return   fullScreenActivity;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(mServiceConnection);
//        mBluetoothLeService = null;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void scanLeDevice(final boolean enable) {
        Handler mHandler = new Handler();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }




    private BluetoothAdapter.LeScanCallback mLeScanCallback =


            new BluetoothAdapter.LeScanCallback() {

                String deviceAliasName;


                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {


                    Thread thread=new Thread(
                    new Runnable() {

                        @Override
                        public void run() {
                            DeviceName deviceAlias=deviceAliasDao.findByName(device.getAddress(),mUserName);
                            if (!(deviceAlias==null))
                                deviceAliasName=deviceAlias.getDeviceAlias();
                            else
                                deviceAliasName=null;

                        }
                    }
                    );


                    thread.start();

                    try {
                        thread.join();
                    }
                    catch (Exception e){

                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            //mLeDeviceListAdapter.addDevice(device);
                            //mLeDeviceListAdapter.notifyDataSetChanged();
                            if (list.size()==0)
                                //Check the database and replace with the


                                list.add(new BleDevice(device,device.getName(),device.getAddress(),deviceAliasName));
                            else {
                                for (int i = 0; i < list.size(); i++) {
                                    if (new String(list.get(i).bleDeviceInfo).equals(device.getAddress()))
                                        break;
                                    else if (i == (list.size() - 1))
                                        list.add(new BleDevice(device,device.getName(), device.getAddress(),deviceAliasName));
                                }
                            }
                            adapter.notifyDataSetChanged();

                            for (int i = 0; i < list.size(); i++)
                                if (list.get(i).connect==true) {
                                    //device.connectGatt(getApplicationContext(), false, service.mGattCallback);
                                    mDeviceAddress=list.get(i).bleDeviceInfo;
                                    //list.get(i).connect=false;
                                    //Go into a thread that keeps on updating the data regarding a given device
                            }
                        }
                    });
                }
            };

    private String checkForDeviceAlias(String deviceId){

            return null;

    }

}
