package eu.iccs.scent.scentmeasure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;

/**
 * Created by theodoropoulos on 4/7/2018.
 */

public class ForgotPassActivity  extends AppCompatActivity {

    static ForgotPassActivity forgotPassActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forgotPassActivity=this;
        setContentView(R.layout.activity_rename);
        WebView myWebView = (WebView) findViewById(R.id.activityRename);
        myWebView.loadUrl("https://www.xteamsoftware.com/scent/wp-login.php?action=lostpassword");

        //Set action bar of the applicatoin
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    public static ForgotPassActivity getInstance(){
        return   forgotPassActivity;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent i=new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
