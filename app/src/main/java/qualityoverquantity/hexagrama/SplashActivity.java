package qualityoverquantity.hexagrama;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {TimeUnit.SECONDS.sleep(5);}
        catch (Exception e) {}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isOnline()) {
                    TextView txInternet = (TextView) findViewById(R.id.txtConection);
                    txInternet.setText(R.string.errorconexion);
                    txInternet.setTextColor(Color.RED);
                } else {
                    Intent mainIntent = new	Intent(SplashActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }
        }, 5000);
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public void checkConnection(){
        if(isOnline()){
            Toast.makeText(SplashActivity.this, "You are connected to Internet", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(SplashActivity.this, "You are not connected to Internet", Toast.LENGTH_SHORT).show();
        }
    }


}
