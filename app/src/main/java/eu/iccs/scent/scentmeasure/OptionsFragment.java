package eu.iccs.scent.scentmeasure;

/**
 * Created by theodoropoulos on 30/11/2017.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by theodoropoulos on 30/11/2017.
 */

/**
 * Created by theodoropoulos on 30/11/2017.
 */

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import eu.iccs.scent.scentmeasure.Data.AppDatabase;
import eu.iccs.scent.scentmeasure.Data.UserOptions;
import eu.iccs.scent.scentmeasure.Data.UserOptionsDao;

// In this case, the fragment displays simple text based on the page
public class OptionsFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static OptionsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        OptionsFragment fragment = new OptionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    AppDatabase db;
    UserOptionsDao userOptionsDao;
    UserOptions userOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);
        //     TextView textView = (TextView) view;
        //     textView.setText("Fragment #" + mPage);


        return view;
    }
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        getActivity().getIntent();
        startActivity(getActivity().getIntent());
        getActivity().finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        final String username = ((SensorMeasurementActivity) getActivity()).mUserName;
        final String email = ((SensorMeasurementActivity) getActivity()).mUserEmail;
        final RadioGroup radioGroup=(RadioGroup) getActivity().findViewById(R.id.radioGroup);
        final RadioButton radioButtonMetric=(RadioButton) getActivity().findViewById(R.id.radioButton);
        RadioButton radioButtonInternational=(RadioButton) getActivity().findViewById(R.id.radioButton2);
        RadioGroup languageGroup=(RadioGroup) getActivity().findViewById(R.id.radioGroupLanguage);
        RadioButton enlishButton=(RadioButton) getActivity().findViewById(R.id.radioButtonEnglish);
        RadioButton greekButton=(RadioButton) getActivity().findViewById(R.id.radioButtonGreek);
        RadioButton romanianButton=(RadioButton) getActivity().findViewById(R.id.radioButtonRomanian);
        SharedPreferences sharedPreferencesLogin = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String locale=  sharedPreferencesLogin.getString("locale", "");
        if (!locale.equals("")){
            if (locale.equals("en"))
                enlishButton.setChecked(true);
            else if (locale.equals("el"))
                greekButton.setChecked(true);
            else
                romanianButton.setChecked(true);
        }
        //Set vies according to options
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        //Here check the username and if it does not exist
                        db = AppDatabase.getDatabase(getActivity());
                        userOptionsDao = db.userOptionsDao();
                        userOptions = userOptionsDao.findByName(username);

                        if (userOptions == null) {

                            DateFormat df = DateFormat.getTimeInstance();
                            df.setTimeZone(TimeZone.getTimeZone("gmt"));
                            String gmtTime = df.format(new Date());
                            ToggleButton uploadMeasurements = (ToggleButton) getActivity().findViewById(R.id.synchButton);
                            boolean measurementsChecked = true;
                            uploadMeasurements.setChecked(measurementsChecked);
                            userOptions = new UserOptions(gmtTime, ((SensorMeasurementActivity) getActivity()).mUserName, measurementsChecked, false, "Metric");
                            userOptionsDao.insertAll(userOptions);
                            radioButtonMetric.setChecked(true);
                        }
                    }
                });
        thread.start();
        try {
            thread.join();
            ToggleButton uploadMeasurements = (ToggleButton) getActivity().findViewById(R.id.synchButton);
            final ToggleButton wifiSynch = (ToggleButton) getActivity().findViewById(R.id.toggleButton2);

            if (userOptions != null) {
                //View upload measurements to wifi toggle button
                uploadMeasurements.setChecked(userOptions.getUploadMeaasurements());
                wifiSynch.setChecked(userOptions.getWifiSynch());
                if (!userOptions.getUploadMeaasurements())
                    wifiSynch.setEnabled(false);
                if (userOptions.getUnits().equals("Metric"))
                {
                    radioButtonMetric.setChecked(true);
                }
                else {
                    radioButtonInternational.setChecked(true);
                }

            } else {
                //timestamp
                //username
                //upload true
                //wifi false
                //
            }
            uploadMeasurements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            final boolean checked = isChecked;

            if (!checked) {
                wifiSynch.setChecked(false);
               wifiSynch.setEnabled(false);

            } else
            {
                wifiSynch.setEnabled(true);

            }
            Thread buttonTHread = new Thread(
                    new Runnable() {
                            @Override
                            public void run() {

                            UserOptions userOptions = userOptionsDao.findByName(username);
                            userOptionsDao.delete(userOptions);
                            userOptions.setUploadMeaasurements(checked);
                            userOptionsDao.insertAll(userOptions);
                            //Get username
                    }
                                  });

            buttonTHread.start();
            try {
                    buttonTHread.join();

                } catch (Exception e) {

                }

            }
            }
            );

            wifiSynch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                              @Override
                                                              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                                                  final boolean checked = isChecked;
                                                                  Thread buttonTHread = new Thread(
                                                                          new Runnable() {
                                                                              @Override
                                                                              public void run() {

                                                                                  UserOptions userOptions = userOptionsDao.findByName(username);
                                                                                  userOptionsDao.delete(userOptions);
                                                                                  userOptions.setWifiSynch(checked);
                                                                                  userOptionsDao.insertAll(userOptions);
                                                                                  //Get username
                                                                              }
                                                                          });

                                                                  buttonTHread.start();
                                                                  try {
                                                                        buttonTHread.join();
                                                                  } catch (Exception e) {

                                                                  }

                                                              }
                                                          }
            );

            //Radio button control
            languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    // find the radiobutton by returned id
                    SharedPreferences sharedPreferencesLogin = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferencesLogin.edit();
                    final RadioButton radioButton = (RadioButton) getActivity().findViewById(checkedId);
                    String radioButtonString=radioButton.getText().toString();
                    Log.d("Debug",radioButtonString);
                    if(radioButtonString.equals( getString(R.string.english))) {
                        setLocale("en");
                        editor.putString("locale", "en");
                        editor.commit();
                    }
                    else if ((radioButtonString.equals(getString(R.string.greek))))
                    {
                        setLocale("el");
                        editor.putString("locale", "el");
                        editor.commit();
                    }
                    else {
                        setLocale("ro");
                        editor.putString("locale", "ro");
                        editor.commit();
                    }
                }});
            //Radio button control
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    // find the radiobutton by returned id
                    final RadioButton radioButton = (RadioButton) getActivity().findViewById(checkedId);

                    Thread buttonTHread = new Thread(
                            new Runnable() {
                                @Override
                                public void run() {

                                    UserOptions userOptions = userOptionsDao.findByName(username);
                                    userOptionsDao.delete(userOptions);
                                    userOptions.setUnits(radioButton.getText().toString());
                                    userOptionsDao.insertAll(userOptions);
                                    //Get username
                                }
                            });

                    buttonTHread.start();
                    try {
                        buttonTHread.join();
                    } catch (Exception e) {

                    }

                }

            });

            //Set usernamen in the respective account
            TextView usernameAccount=(TextView) getActivity().findViewById(R.id.accountUsername);
            usernameAccount.setText(username);
            //Set email in the respective account
            TextView emailAccount=(TextView) getActivity().findViewById(R.id.accountEmail);
            emailAccount.setText(email);
            //Set the logout button listener
            Button logoutButton= (Button) getActivity().findViewById(R.id.logoutButtonAccount);
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
            //set logout status to false and go to the initial login screen
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(SensorMeasurementActivity.LOGGEDIN,false);
                    editor.commit();
                    Intent mainIntent = new Intent(getActivity(), LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    getActivity().finish();
            //Load the login activity
                }
            });


        } catch (Exception e) {
            Toast.makeText(getContext(), "exception", Toast.LENGTH_SHORT).show();
        }
    }
}
