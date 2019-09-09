package eu.iccs.scent.scentmeasure;

/**
 * Created by theodoropoulos on 30/11/2017.
 */

import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import eu.iccs.scent.scentmeasure.Data.AppDatabase;
import eu.iccs.scent.scentmeasure.Data.Coordinates;
import eu.iccs.scent.scentmeasure.Data.DeviceAliasDao;
import eu.iccs.scent.scentmeasure.Data.DeviceName;
import eu.iccs.scent.scentmeasure.Data.UserOptions;
import eu.iccs.scent.scentmeasure.Data.UserOptionsDao;


// In this case, the fragment displays simple text based on the page
public class TemperatureFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public String mDeviceAlias;

    private int mPage;
    AppDatabase db;
    UserOptionsDao userOptionsDao;
    UserOptions userOptions;
    private TextView takingMeasurementsTemp;
    private ProgressBar progressBarIndefinite;
    private int progressStatus=-1;

    public static TemperatureFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        TemperatureFragment fragment = new TemperatureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperature, container, false);

        //     TextView textView = (TextView) view;
        //     textView.setText("Fragment #" + mPage);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Button button= (Button) getActivity().findViewById(R.id.buttonStartTemperature);
        if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false)
            button.setText(R.string.start);
        else
            button.setText(R.string.stopButton);
        //Setting dates of the view. And setting min max values for
        //temperature and moisture
        setDates(); // this is called ...

    }

    @Override
    public void onResume() {
        super.onResume();
        setDates(); // this is called ...
        SharedPreferences minMaxValues = getActivity().getSharedPreferences(((SensorMeasurementActivity)getActivity()).PREFS_NAME, 0);
        String lastMeasurement=minMaxValues.getString(((SensorMeasurementActivity)getActivity()).LAST_MEASUREMENTS,"");

        String lastLocation=minMaxValues.getString(((SensorMeasurementActivity)getActivity()).LAST_LOCATION,"");
        String lastPosition=minMaxValues.getString(((SensorMeasurementActivity)getActivity()).LAST_POSITION,"");

        TextView username= (TextView) getActivity().findViewById(R.id.textUsernameTemperature);
        username.setText( (((SensorMeasurementActivity) getActivity())).mUserName);

        TextView userScore= (TextView) getActivity().findViewById(R.id.trustLevelTemperature);
        userScore.setText(minMaxValues.getString(((SensorMeasurementActivity)getActivity()).USERSCORE,""));
        takingMeasurementsTemp = (TextView) getActivity().findViewById(R.id.takingMeasurementsTemp);
        progressBarIndefinite = (ProgressBar) getActivity().findViewById(R.id.loadingTemperatureProgressBar);

        SharedPreferences loginSharedPreferences= getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
       /*
        if (appMode.equals("explore")) {
            int measurementCount=loginSharedPreferences.getInt(LoginActivity.MEASUREMENTCOUNT, 0);
            progressBar.setProgress(measurementCount % 5);
        }else{
            progressBar.setProgress(((SensorMeasurementActivity) getActivity()).measurementsAll % 5);
        }
        */
        //Registering button listener
        final Button button= (Button) getActivity().findViewById(R.id.buttonStartTemperature);
        TextView synchingTextView= (TextView) getActivity().findViewById(R.id.synchingText);

        if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false){
            button.setText(R.string.start);
            synchingTextView.setText(R.string.waiting);
            takingMeasurementsTemp.setVisibility(View.GONE);
            progressBarIndefinite.setVisibility(View.GONE);
        }
        else {
            button.setText(R.string.stopButton);
            synchingTextView.setText(R.string.synching);
            takingMeasurementsTemp.setVisibility(View.VISIBLE);
            loginSharedPreferences= getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
            appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
            progressBarIndefinite.setVisibility(View.VISIBLE);
            if (appMode.equals("explore")) {
                button.setVisibility(View.GONE);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SensorMeasurementActivity) getActivity()).measurementsCollected==false) {
                    ((SensorMeasurementActivity) getActivity()).startMeasuring();
                    SharedPreferences loginSharedPreferences= getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                    takingMeasurementsTemp.setVisibility(View.VISIBLE);
                    progressBarIndefinite.setVisibility(View.VISIBLE);


                    String appMode=loginSharedPreferences.getString(LoginActivity.APPMODE, "");
                    if (appMode.equals("explore")){
                        button.setVisibility(View.GONE);
                    }
                }
                else {
                    ((SensorMeasurementActivity) getActivity()).stopMeasuring();
                    takingMeasurementsTemp.setVisibility(View.GONE);
                    progressBarIndefinite.setVisibility(View.GONE);


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

        Button sensorSettings=(Button) getActivity().findViewById(R.id.sensorSettingsTemperature);
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
        TextView viewToday= (TextView) getActivity().findViewById(R.id.todaysDateTemp);
        viewToday.setText("Today");
        TextView previousDate= (TextView) getActivity().findViewById(R.id.previousDateTemp);
        TextView previousTwoDate= (TextView) getActivity().findViewById(R.id.previousTwoDateTemp);
        TextView previousThreeDate= (TextView) getActivity().findViewById(R.id.previousThreeDateTemp);
        TextView[] views={viewToday, previousDate, previousTwoDate, previousThreeDate  };

        updateUITemperature();

        for (int i=1;i<4;i++)
        {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date dateYesterday = calendar.getTime();
            views[i].setText(new SimpleDateFormat("EE", Locale.ENGLISH).format(dateYesterday.getTime()));
        }

        // 3 letter name form of the day

    }

    public void updateUITemperature(){
        //Get viewa of the temperature fragment and update them accordingly
        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        Date currentTime = Calendar.getInstance().getTime();
        String dateStr = date.format(currentTime);
        //Get minimum and maximum values for temperature and load them in the respective view
        SharedPreferences minMaxValues = getActivity().getSharedPreferences(((SensorMeasurementActivity)getActivity()).PREFS_NAME, 0);
        //Check current minimum and maximum values for temperature and moisture
        String minimumTemp = (minMaxValues.getString(((SensorMeasurementActivity)getActivity()).MIN_TEMP + dateStr, "10000.0" ));
        String maximumTemp = (minMaxValues.getString(((SensorMeasurementActivity)getActivity()).MAX_TEMP + dateStr, "-10000.0" ));
        TextView todaysMinMax=(TextView)getActivity().findViewById(R.id.todaysMinMax);

        if (minimumTemp=="10000.0")
            minimumTemp="_ _";
        if (maximumTemp=="-10000.0")
            maximumTemp="_ _";


        todaysMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));


    }

    public void setSensorUI(SensorMeasurements sensorMeasurements) {
        TextView text = (TextView) getActivity().findViewById(R.id.temperature);
        TextView textMoisture = (TextView) getActivity().findViewById(R.id.moisture);
        TextView timestampTemperature = (TextView) getActivity().findViewById(R.id.textFrequency);
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String time = df.format(currentTime);
        timestampTemperature.setText(getString(R.string.update) + " " + sensorMeasurements.timestamp);

        //Check which unit we are working in and then update the UI. If it is metric
        //leave everything as is. If it is International then update to Farhenheit

        Thread buttonTHread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        db = AppDatabase.getDatabase(getActivity());
                        userOptionsDao = db.userOptionsDao();
                        userOptions = userOptionsDao.findByName(((SensorMeasurementActivity)getActivity()).mUserName);
                        //Get username
                    }
                });

        buttonTHread.start();
        try {
            buttonTHread.join();
        } catch (Exception e) {

        }

        if (userOptions.getUnits().equals("Metric")){
            ;//do nothing
            text.setText(String.format( "%1$,.2f",sensorMeasurements.temperature).substring(0, 4) + "\u00B0");
            //Check current minimum and maximum values for temperature and moisture
            updateMinMaxTemperature(sensorMeasurements);
        }else
        {
            //Convert data to farhenheit
            text.setText(String.valueOf( convertCelciusToFahrenheit(sensorMeasurements.temperature)).substring(0, 4) + "\u00B0");
            updateMinMaxTemperatureInternational(sensorMeasurements);
        }


    }

    public void updateStatusBar(int progressStatus){
        //Update the status bar when
       // progressBar.setProgress(progressStatus);
    }

    private void updateMinMaxTemperature(SensorMeasurements sensorMeasurements){

        Date currentTime = Calendar.getInstance().getTime();
        SharedPreferences minMaxValues = getActivity().getSharedPreferences( ((SensorMeasurementActivity) getActivity()).PREFS_NAME, 0);
        SharedPreferences.Editor editor = minMaxValues.edit();
        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        String dateStr = date.format(currentTime);
        String minimumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MIN_TEMP + dateStr, "10000.0"));
        String maximumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MAX_TEMP + dateStr, "-10000.0"));

        if (minimumTemp=="10000.0")
            minimumTemp="_ _";
        if (maximumTemp=="-10000.0")
            maximumTemp="_ _";

        TextView todaysMinMax= (TextView) getActivity().findViewById(R.id.todaysMinMax);
        todaysMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));

        //Setup the views of previous days
        for (int daysBehind =1;daysBehind<4;daysBehind++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -daysBehind);
            currentTime =  cal.getTime();
            date = new SimpleDateFormat("yyyy/MM/dd");
            dateStr = date.format(currentTime);
            minimumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MIN_TEMP + dateStr, "10000.0"));
            maximumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MAX_TEMP + dateStr, "-10000.0"));

            if (minimumTemp == "10000.0")
                minimumTemp = "_ _";
            if (maximumTemp == "-10000.0")
                maximumTemp = "_ _";

            if (daysBehind==1) {
                TextView previousMinMax = (TextView) getActivity().findViewById(R.id.previousMinMax);
                previousMinMax.setText(maximumTemp.substring(0, maximumTemp.length() < 4 ? maximumTemp.length() : 4) + "\n" + minimumTemp.substring(0, minimumTemp.length() < 4 ? minimumTemp.length() : 4));
                }
            else if (daysBehind==2){
                //Setup the views of day-2
                TextView previoustwoMinMax= (TextView) getActivity().findViewById(R.id.previousTwoMinMax);
                previoustwoMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));
            }else if (daysBehind==3){
                //setup the views of day-3
                TextView previousThreeMinMax= (TextView) getActivity().findViewById(R.id.previousThreeMinMax);
                previousThreeMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));

            }
        }
    }

    private void updateMinMaxTemperatureInternational(SensorMeasurements sensorMeasurements){

        Date currentTime = Calendar.getInstance().getTime();
        SharedPreferences minMaxValues = getActivity().getSharedPreferences( ((SensorMeasurementActivity) getActivity()).PREFS_NAME, 0);
        SharedPreferences.Editor editor = minMaxValues.edit();
        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        String dateStr = date.format(currentTime);
        String minimumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MIN_TEMP + dateStr, "10000.0"));
        String maximumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MAX_TEMP + dateStr, "-10000.0"));

        if (minimumTemp=="10000.0")
            minimumTemp="_ _";
        else {
            minimumTemp= String.valueOf(convertCelciusToFahrenheit(Double.valueOf(minimumTemp)));
        }

        if (maximumTemp=="-10000.0")
            maximumTemp="_ _";
        else{
            maximumTemp=String.valueOf(convertCelciusToFahrenheit(Double.valueOf(maximumTemp)));

        }

        TextView todaysMinMax= (TextView) getActivity().findViewById(R.id.todaysMinMax);
        todaysMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));

        //Setup the views of previous days
        for (int daysBehind =1;daysBehind<4;daysBehind++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -daysBehind);
            currentTime =  cal.getTime();
            date = new SimpleDateFormat("yyyy/MM/dd");
            dateStr = date.format(currentTime);
            minimumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MIN_TEMP + dateStr, "10000.0"));
            maximumTemp = (minMaxValues.getString(((SensorMeasurementActivity) getActivity()).MAX_TEMP + dateStr, "-10000.0"));

            if (minimumTemp=="10000.0")
                minimumTemp="_ _";
            else {
                minimumTemp= String.valueOf(convertCelciusToFahrenheit(Double.valueOf(minimumTemp)));
            }

            if (maximumTemp=="-10000.0")
                maximumTemp="_ _";
            else{
                maximumTemp=String.valueOf(convertCelciusToFahrenheit(Double.valueOf(maximumTemp)));

            }

            if (daysBehind==1) {
                TextView previousMinMax = (TextView) getActivity().findViewById(R.id.previousMinMax);
                previousMinMax.setText(maximumTemp.substring(0, maximumTemp.length() < 4 ? maximumTemp.length() : 4) + "\n" + minimumTemp.substring(0, minimumTemp.length() < 4 ? minimumTemp.length() : 4));
            }
            else if (daysBehind==2){
                //Setup the views of day-2
                TextView previoustwoMinMax= (TextView) getActivity().findViewById(R.id.previousTwoMinMax);
                previoustwoMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));
            }else if (daysBehind==3){
                //setup the views of day-3
                TextView previousThreeMinMax= (TextView) getActivity().findViewById(R.id.previousThreeMinMax);
                previousThreeMinMax.setText(maximumTemp.substring(0,maximumTemp.length()<4 ? maximumTemp.length():4 )+"\n"+minimumTemp.substring(0,minimumTemp.length()<4 ? minimumTemp.length():4));

            }
        }



    }

    public void updatePosition(double longitude, double latitude){
        TextView gps = (TextView) getActivity().findViewById(R.id.gpsTemperature);
        gps.setText(String.valueOf(longitude) + ", "+String.valueOf(latitude));
    }

    public void updateLocation(String location){
        TextView llTemperature = (TextView) getActivity().findViewById(R.id.livingLabTemperature);
        llTemperature.setText(location);
    }

    private void showDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.rename_dialog);

        // set the custom dialog components - text, image and button
        ImageButton close = (ImageButton) dialog.findViewById(R.id.btnClose);
        Button buy = (Button) dialog.findViewById(R.id.btnBuy);
        final TextView macText= (TextView) dialog.findViewById(R.id.macAddressID);
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

        // Save Button
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save alias in the list
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
                //TODO Buy button action
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

    // Converts to celcius
    private double convertFahrenheitToCelcius(double fahrenheit) {
        return ((fahrenheit - 32.0) * 5.0 / 9.0);
    }

    // Converts to fahrenheit
    private double convertCelciusToFahrenheit(double celsius) {
        return ((celsius * 9.0) / 5.0) + 32.0;
    }


    public void setSensorCollectingMeasurements(boolean start) {
        Button startMoisture= (Button) getActivity().findViewById(R.id.buttonStartTemperature);
        TextView synchingTextView= (TextView) getActivity().findViewById(R.id.synchingText);
        if (start) {
            startMoisture.setText(R.string.stopButton);
            synchingTextView.setText(R.string.synching);
        }
        else {
            startMoisture.setText(R.string.start);
            synchingTextView.setText(R.string.waiting);
        }
    }
}