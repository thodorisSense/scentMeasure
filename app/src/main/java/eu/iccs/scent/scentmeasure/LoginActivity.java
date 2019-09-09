package eu.iccs.scent.scentmeasure;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.iccs.scent.scentmeasure.Data.Coordinates;
import eu.iccs.scent.scentmeasure.Data.Measurement;
import eu.iccs.scent.scentmeasure.Data.NetworkPacket;
import eu.iccs.scent.scentmeasure.Data.ScentData;
import eu.iccs.scent.scentmeasure.Data.UserScore;
import eu.iccs.scent.scentmeasure.Data.XTeamLogin;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.READ_CONTACTS;
import static android.R.attr.targetSdkVersion;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View registerView;
    private View forgotPassView;
    public static final String APPMODE = "appMode";
    public static final String PREFS_NAME = "appMode";
    public static final String MEASUREMENTCOUNT = "measCount";
    private static final int MY_PERMISSIONS_INTERNET = 1;
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 2;
    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 3;
    private static final int MY_PERMISSIONS_BLUETOOTH = 4;
    private static final int MY_PERMISSIONS_BLUETOOTH_ADMIN= 5;
    private static final int MY_PERMISSIONS_NETWORK_STATE= 9;
    private static final int MY_PERMISSIONS_SYSTEM_ALERT_WINDOW= 10;
    public static final int MULTIPLE_PERMISSIONS = 100;

    protected void setLocale(String locale){
        Locale myLocale = new Locale(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

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
            int debug=0;
        }
    }

    protected void requestPermissions(){
        //Request all permissions for the app.



    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},
                MY_PERMISSIONS_INTERNET);
        SharedPreferences sharedPreferencesLogin = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferencesLogin.edit();
        //SDet locale of the app
        //Get default locale if the overidden locale is null
        String locale=sharedPreferencesLogin.getString("locale","");
        if (locale.equals("")){

            locale=Locale.getDefault().getLanguage();
            if (locale.equals("en")||locale.equals("el")||locale.equals("ro")) {
                editor.putString("locale", locale);
                editor.commit();
                setLocale(locale);
            }else
            {
                editor.putString("locale", "en");
                editor.commit();
                setLocale("en");

            }

        }else {
            setLocale(locale);
        }

        setContentView(R.layout.activity_login);
        //Kill any possible previous activity
        //try {
        //    SensorMeasurementActivity instance=SensorMeasurementActivity.getInstance();
        //    if (instance!=null)
        //        instance.finish();
        //}catch(Exception e){

        //}

        editor.putString(LoginActivity.APPMODE,"standalone");
        editor.commit();

        //Initialising explore to null
        scentExploreHandle();

        //editor.putString(LoginActivity.APPMODE,"standalone");
        //If the stay loggend in options is enabled check that a
        //a user exists and then go directly to the bluetooth device
        //list
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        registerView = (TextView) findViewById(R.id.registerView);
        registerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: register the new account here.
                Intent webIntent = new Intent(LoginActivity.this, WebActivity.class);
                startActivity(webIntent);


            }
        });

        forgotPassView = (TextView) findViewById(R.id.forgotPassView);
        forgotPassView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: register the new account here.
                Intent webIntent = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivity(webIntent);


            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // refresh your views here
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
        scentExploreHandle();

    }

    public void scentExploreHandle(){

        SharedPreferences sharedPreferencesLogin = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String appMode=sharedPreferencesLogin.getString(LoginActivity.APPMODE,"");
        //Get intent from launching activity
        Intent intent=getIntent();
        intent.getComponent();
        Bundle extras=intent.getExtras();

        if (extras!=null) {
            for (String key : extras.keySet()) {
                if (key.toString().equals("arguments")){
                    //Set application to scent explore mode
                    Object value = extras.get(key);
                    Log.d("Debug", String.format("%s %s (%s)", key,
                            value.toString(), value.getClass().getName()));
                    //Take comma seperated csv and put values into memory
                    Reader inputString=new StringReader(value.toString());
                    BufferedReader br = new BufferedReader(inputString);
                    String str="";
                    Map<String,String> exploreDataMap = new HashMap<String,String>();
                    try {
                        str = br.readLine();
                    }catch (Exception e ){

                    }

                    List<String> exploreData= Arrays.asList(str.split(","));
                    for (int i=0;i<exploreData.size();i++) {
                        Log.d("Debugging",exploreData.get(i));
                       //Toast.makeText(this,exploreData.get(i),Toast.LENGTH_SHORT).show();
                        exploreDataMap.put(exploreData.get(i).split(":")[0], exploreData.get(i).split(":")[1]);
                    }

                    sharedPreferencesLogin = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferencesLogin.edit();
                    editor.putString(LoginActivity.APPMODE,"explore");
                    editor.putInt(LoginActivity.MEASUREMENTCOUNT, 0);
                    editor.commit();
                    editor.commit();
                    appMode=sharedPreferencesLogin.getString(LoginActivity.APPMODE,"");
                    //Store userbame here
                    SharedPreferences sharedPreferences = getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editorMeasurements = sharedPreferences.edit();
                    String userId=exploreDataMap.get("userId");
                    String poiId=exploreDataMap.get("poi");
                    String userName=exploreDataMap.get("username");
                    String score=exploreDataMap.get("score");
                    editorMeasurements.putString(SensorMeasurementActivity.USERNAME, userName);
                    editorMeasurements.putString(SensorMeasurementActivity.POIID, poiId);
                    editorMeasurements.putString(SensorMeasurementActivity.EMAIL,"");
                    editorMeasurements.putInt(SensorMeasurementActivity.USERID,Integer.parseInt(userId));
                    editorMeasurements.putString(SensorMeasurementActivity.USERSCORE,score);
                    editorMeasurements.putBoolean(SensorMeasurementActivity.LOGGEDIN,true);

                    editorMeasurements.commit();
                    //Klll previous activity if it exists
                    Intent mainIntent = new Intent(LoginActivity.this, FullscreenActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    break;
                }

            }
        }
        else
            ;//
        // Toast.makeText(this,"Extras is null", Toast.LENGTH_LONG).show();

        if (appMode.equals("standalone")) {

            SharedPreferences sharedPreferences = getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
            String usernamStr = sharedPreferences.getString(SensorMeasurementActivity.USERNAME, null);


            if (usernamStr==null) {
                //do nothing go on
            } else {

                // check loggedin status
                Boolean loggedin = sharedPreferences.getBoolean(SensorMeasurementActivity.LOGGEDIN, false);
                if (loggedin.equals(false)) {
                } else {
                    Intent mainIntent = new Intent(LoginActivity.this, FullscreenActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(this);
        //Run this when on resume happens
        //Toast.makeText(this,"IN RESUME",Toast.LENGTH_LONG ).show();
        //Handle intents from scent explore
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        //We done check for emails. The username can be of any type

        else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        //return password.length() > 4;
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Boolean loginStatus=false;

                //Send login request to backend and
                //and get an object containing user auth
                loginStatus=sendDataToBackend();

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            //Wait for the network request to end here and finish

            return loginStatus;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {

              //Customize according to details for now do noithing
               // mPasswordView.setError(getString(R.string.error_incorrect_password));
               // mPasswordView.requestFocus();
            }
        }



        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }



        private Boolean sendDataToBackend(){

            String url = "https://www.xteamsoftware.com/scent/api/auth/generate_auth_cookie/?username="+mEmail+"&password="+mPassword;

            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest request = new StringRequest(Request.Method.GET, url, future, future);
            request.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

            try {
                String result = future.get();
                Gson gson=new Gson();
                XTeamLogin xteamLogin=gson.fromJson(result,XTeamLogin.class);
                if (xteamLogin.status.equals("ok"))
                {
                    //Store userbame here
                    SharedPreferences sharedPreferences = getSharedPreferences(SensorMeasurementActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SensorMeasurementActivity.USERNAME,xteamLogin.user.username);
                    editor.putString(SensorMeasurementActivity.EMAIL,xteamLogin.user.email);
                    editor.putInt(SensorMeasurementActivity.USERID,xteamLogin.user.id);
                    editor.putBoolean(SensorMeasurementActivity.LOGGEDIN,true);
                    editor.commit();

                    //Create request for the score wait for result and go to next screen anyways
                    String urlScore="https://www.xteamsoftware.com/scent/functions/get_statistic.php?id=2&nickname="+xteamLogin.user.username;
                    future = RequestFuture.newFuture();
                    request = new StringRequest(Request.Method.GET, urlScore, future, future);
                    request.setRetryPolicy(new DefaultRetryPolicy(10000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    NetSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
                    result = future.get();
                    gson=new Gson();
                    UserScore score=gson.fromJson(result,UserScore.class);
                    editor.putString(SensorMeasurementActivity.USERSCORE,score.getScore());
                    editor.commit();
                    Intent mainIntent = new Intent(LoginActivity.this, FullscreenActivity.class);
                    startActivity(mainIntent);
                    return true;
                }else {
                    //do nothing
                    return false;
                }

            }
            catch (Exception e){
                return false;
            }
        }
        }
}

