package eu.iccs.scent.scentmeasure;

/**
 * Created by theodoropoulos on 30/11/2017.
 */

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by theodoropoulos on 30/11/2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import eu.iccs.scent.scentmeasure.Data.AppDatabase;
import eu.iccs.scent.scentmeasure.Data.Coordinates;
import eu.iccs.scent.scentmeasure.Data.DeviceAliasDao;
import eu.iccs.scent.scentmeasure.Data.DeviceName;

// In this case, the fragment displays simple text based on the page
public class MoistureFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public String mDeviceAlias;
    private TextView takingMeasMoist;
    private ProgressBar progressBarMoisture;
    private int progressStatus=-1;

    private int mPage;

    public static MoistureFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        MoistureFragment fragment = new MoistureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moisture, container, false);
        //     TextView textView = (TextView) view;
        //     textView.setText("Fragment #" + mPage);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setDates(); // this is called ...      //Registering button listener
        Button button= (Button) getActivity().findViewById(R.id.sensorStartMoisture);
        if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false)
            button.setText(R.string.start);
        else
            button.setText(R.string.stopButton);

    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed() && getView()!=null)
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setDates(); // this is called ...
        SharedPreferences minMaxValues = getActivity().getSharedPreferences(((SensorMeasurementActivity)getActivity()).PREFS_NAME, 0);
        String lastMeasurement=minMaxValues.getString(((SensorMeasurementActivity)getActivity()).LAST_MEASUREMENTS,"");
        String lastLocation=minMaxValues.getString(((SensorMeasurementActivity)getActivity()).LAST_LOCATION,"");
        String lastPosition=minMaxValues.getString(((SensorMeasurementActivity)getActivity()).LAST_POSITION,"");

        TextView username= (TextView) getActivity().findViewById(R.id.textUsernameMoisture);
        username.setText( (((SensorMeasurementActivity) getActivity())).mUserName);

        progressBarMoisture = (ProgressBar) getActivity().findViewById(R.id.loadingMoistureProgressBar);


        TextView userScore= (TextView) getActivity().findViewById(R.id.trustLevelMoisture);
        userScore.setText(minMaxValues.getString(((SensorMeasurementActivity)getActivity()).USERSCORE,""));
        SharedPreferences loginSharedPreferences= getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        takingMeasMoist = (TextView) getActivity().findViewById(R.id.takingMeasurementsMoist);
        String appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
        /*
        if (appMode.equals("explore")) {
            int measurementCount=loginSharedPreferences.getInt(LoginActivity.MEASUREMENTCOUNT, 0);
            takingMeasMoist.setProgress(measurementCount % 5);
        }else{
            progressBar.setProgress(((SensorMeasurementActivity) getActivity()).measurementsAll % 5);
        }
*/

        //Registering button listener
        final Button button= (Button) getActivity().findViewById(R.id.sensorStartMoisture);
        TextView textViewSynching= (TextView) getActivity().findViewById(R.id.synchingT);
        if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false){
            button.setText(R.string.start);
            textViewSynching.setText(R.string.waiting);
            takingMeasMoist.setVisibility(View.GONE);
            progressBarMoisture.setVisibility(View.GONE);
        }
        else {
            button.setText(R.string.stopButton);
            textViewSynching.setText(R.string.synching);
            takingMeasMoist.setVisibility(View.VISIBLE);
            progressBarMoisture.setVisibility(View.VISIBLE);
            loginSharedPreferences= getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
            appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
            if (appMode.equals("explore")) {
                button.setVisibility(View.GONE);
            }

        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false) {
                    ((SensorMeasurementActivity) getActivity()).startMeasuring();
                    takingMeasMoist.setVisibility(View.VISIBLE);
                    progressBarMoisture.setVisibility(View.VISIBLE);

                    if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false) {
                        progressBarMoisture.setVisibility(View.VISIBLE);
                        ((SensorMeasurementActivity) getActivity()).startMeasuring();
                        SharedPreferences loginSharedPreferences= getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                        String appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");

                        if (appMode.equals("explore")){
                            button.setVisibility(View.GONE);
                        }
                    }
                }
                else {
                    ((SensorMeasurementActivity) getActivity()).stopMeasuring();
                    takingMeasMoist.setVisibility(View.GONE);
                    progressBarMoisture.setVisibility(View.GONE);

                }
            }
        });



        Gson gson=new Gson();
        if (lastMeasurement!="")
        {
            SensorMeasurements scentMeasurement=gson.fromJson(lastMeasurement, SensorMeasurements.class);
            setSensorUI(scentMeasurement);
        }


        if (!lastPosition.equals(""))
        {
            Coordinates scentPosition=gson.fromJson(lastPosition, Coordinates.class);
            updatePosition(scentPosition.getLongitude(), scentPosition.getLatitude());
        }

        if (!lastLocation.equals(""))
        {
            String address=gson.fromJson(lastLocation, String.class);
            updateLocation(address);

        }

        Button sensorSettings=(Button) getActivity().findViewById(R.id.sensorSettingsMoisture);
        sensorSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        SharedPreferences userAttrs = getActivity().getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
        final String mUserName=userAttrs.getString(SensorMeasurementActivity.USERNAME, "unknown");

        Thread thread=new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase db = AppDatabase.getDatabase(getActivity());
                        DeviceAliasDao deviceAliasDao = db.deviceAliasDao();
                        DeviceName device=deviceAliasDao.findByName(((SensorMeasurementActivity) getActivity()).mDeviceAddress,mUserName);
                        if (!(device==null))
                            mDeviceAlias=device.getDeviceAlias();
                        //Get username
                    }
                }
        );
        thread.start();
        try {
            thread.join();
        }
        catch (Exception e){

        }

        }


    private void setDates(){

        Calendar calendar = Calendar.getInstance();
        TextView viewToday= (TextView) getActivity().findViewById(R.id.todaysDateMoist);
        viewToday.setText("Today");
        TextView previousDate= (TextView) getActivity().findViewById(R.id.previousDateMoist);
        TextView previousTwoDate= (TextView) getActivity().findViewById(R.id.previousTwoDateMoist);
        TextView previousThreeDate= (TextView) getActivity().findViewById(R.id.previousThreeDateMoist);
        TextView[] views={viewToday, previousDate, previousTwoDate, previousThreeDate  };

        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        Date currentTime = Calendar.getInstance().getTime();
        String dateStr = date.format(currentTime);

        /*
        //Get minimum and maximum values for temperature and load them in the respective view
        SharedPreferences minMaxValues = getActivity().getSharedPreferences(((SensorMeasurementActivity)getActivity()).PREFS_NAME, 0);
        //Check current minimum and maximum values for temperature and moisture
        String minimumMoist = (minMaxValues.getString(((SensorMeasurementActivity)getActivity()).MIN_MOIST + dateStr, "10000.0" ));
        String maximumMoist = (minMaxValues.getString(((SensorMeasurementActivity)getActivity()).MAX_MOIST + dateStr, "-10000.0" ));



        TextView todaysMinMaxMoist=(TextView)getActivity().findViewById(R.id.todaysMinMaxMoist);

        todaysMinMaxMoist.setText(maximumMoist.substring(0,maximumMoist.length()<4 ? maximumMoist.length():4)+"\n"+minimumMoist.substring(0,minimumMoist.length()<4 ? minimumMoist.length():4));
*/
        for (int i=1;i<4;i++)
        {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date dateYesterday = calendar.getTime();
            views[i].setText(new SimpleDateFormat("EE", Locale.ENGLISH).format(dateYesterday.getTime()));

        }

        // 3 letter name form of the day

    }

    public void setSensorUI(SensorMeasurements sensorMeasurements) {
        TextView textMoisture = (TextView) getActivity().findViewById(R.id.moisture);
        TextView timestampFrequencyMoisture = (TextView) getActivity().findViewById(R.id.textFrequencyMoisture);
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String time = df.format(currentTime);
        timestampFrequencyMoisture.setText(getString(R.string.update) + " " + sensorMeasurements.timestamp);




        textMoisture.setText(String.valueOf(sensorMeasurements.humidity) + "%");

        //Check current minimum and maximum values for temperature and moisture
        updateMinMaxMoisture(sensorMeasurements);
//Update everything here and get the ovreall status from the activity

    }

    private void updateMinMaxMoisture(SensorMeasurements sensorMeasurements){

        Date currentTime = Calendar.getInstance().getTime();
        SharedPreferences minMaxValues = getActivity().getSharedPreferences( ((SensorMeasurementActivity)getActivity()).PREFS_NAME, 0);
        SharedPreferences.Editor editor = minMaxValues.edit();
        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        String dateStr = date.format(currentTime);

        String minimumMoist = (minMaxValues.getString(((SensorMeasurementActivity)getActivity()).MIN_MOIST + dateStr, "10000.0"));
        String maximumMoist = (minMaxValues.getString(((SensorMeasurementActivity)getActivity()).MAX_MOIST + dateStr, "-10000.0"));

        if (minimumMoist=="10000.0")
            minimumMoist="_ _";
        if (maximumMoist=="-10000.0")
            maximumMoist="_ _";


        TextView todaysMinMaxMoist= (TextView) getActivity().findViewById(R.id.todaysMinMaxMoist);
        todaysMinMaxMoist.setText(maximumMoist.substring(0,maximumMoist.length()<4 ? maximumMoist.length():4)+"\n"+minimumMoist.substring(0,minimumMoist.length()<4 ? minimumMoist.length():4));

        //Setup the views of previous days
        for (int daysBehind =1;daysBehind<4;daysBehind++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -daysBehind);
            currentTime =  cal.getTime();
            date = new SimpleDateFormat("yyyy/MM/dd");
            dateStr = date.format(currentTime);
            minimumMoist = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MIN_MOIST + dateStr, "10000.0"));
            maximumMoist = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MAX_MOIST + dateStr, "-10000.0"));

            if (minimumMoist == "10000.0")
                minimumMoist = "_ _";
            if (maximumMoist == "-10000.0")
                maximumMoist = "_ _";

            if (daysBehind==1) {
                TextView previousMinMax = (TextView) getActivity().findViewById(R.id.previousMinMaxMoist);
                previousMinMax.setText(maximumMoist.substring(0, maximumMoist.length() < 4 ? maximumMoist.length() : 4) + "\n" + minimumMoist.substring(0, minimumMoist.length() < 4 ? minimumMoist.length() : 4));
            }
            else if (daysBehind==2){
                //Setup the views of day-2
                TextView previoustwoMinMax= (TextView) getActivity().findViewById(R.id.previousTwoMinMaxMoist);
                previoustwoMinMax.setText(maximumMoist.substring(0,maximumMoist.length()<4 ? maximumMoist.length():4 )+"\n"+minimumMoist.substring(0,minimumMoist.length()<4 ? minimumMoist.length():4));
            }else if (daysBehind==3){
                //setup the views of day-3
                TextView previousThreeMinMax= (TextView) getActivity().findViewById(R.id.previousThreeMinMaxMoist);
                previousThreeMinMax.setText(maximumMoist.substring(0,maximumMoist.length()<4 ? maximumMoist.length():4 )+"\n"+minimumMoist.substring(0,minimumMoist.length()<4 ? minimumMoist.length():4));

            }
        }


    }

    public void updatePosition(double longitude, double latitude){
        TextView gpsMoisture = (TextView) getActivity().findViewById(R.id.gpsLogoMoisture);
        gpsMoisture.setText(String.valueOf(longitude) + ", "+String.valueOf(latitude));

    }

    public void updateLocation(String location){
        TextView llMoisture = (TextView) getActivity().findViewById(R.id.livingLabMoisture);
        llMoisture.setText(location);

    }

    private void showDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.rename_dialog);

        // set the custom dialog components - text, image and button
        ImageButton close = (ImageButton) dialog.findViewById(R.id.btnClose);
        Button buy = (Button) dialog.findViewById(R.id.btnBuy);

        TextView macText= (TextView) dialog.findViewById(R.id.macAddressID);
        macText.setText( ((SensorMeasurementActivity) getActivity()).mDeviceAddress);

        final EditText alias= (EditText)   dialog.findViewById(R.id.deviceAlias);
        if (!(mDeviceAlias==null))
            alias.setText(mDeviceAlias);

        // Close Button
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //TODO Close button action
            }
        });

        // Buy Button
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread=new Thread(

                        new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase db = AppDatabase.getDatabase(getActivity());
                                DeviceAliasDao deviceAliasDao = db.deviceAliasDao();
                                DeviceName device=deviceAliasDao.findByName(((SensorMeasurementActivity) getActivity()).mDeviceAddress,((SensorMeasurementActivity) getActivity()).mUserName);
                                if (!(device==null))
                                    deviceAliasDao.delete(device);
                                //Get username
                                //TODO: Implement database functionality
                                DateFormat df = DateFormat.getTimeInstance();
                                df.setTimeZone(TimeZone.getTimeZone("gmt"));
                                String gmtTime = df.format(new Date());
                                device=new DeviceName( gmtTime ,((SensorMeasurementActivity) getActivity()).mUserName,((SensorMeasurementActivity) getActivity()).mDeviceAddress, alias.getText().toString());
                                deviceAliasDao.insertAll(device);
                                mDeviceAlias=device.getDeviceAlias();

                            }
                        }
                );
                thread.start();
                try {
                    thread.join();
                }
                catch (InterruptedException e){
                    ;
                }

                //Save item to the list of
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void updateStatusBar(int progressStatus){
        //Update the status bar when
        //progressBar.setProgress(progressStatus);
    }

    public void setSensorCollectingMeasurements(boolean start) {
        Button startMoisture= (Button) getActivity().findViewById(R.id.sensorStartMoisture);
        TextView textViewSynching= (TextView) getActivity().findViewById(R.id.synchingT);

        if (start){
            startMoisture.setText(R.string.stopButton);
            textViewSynching.setText(R.string.synching);
        }
        else {
            startMoisture.setText(R.string.start);
            textViewSynching.setText(R.string.waiting);
        }
    }
}