package qualityoverquantity.hexagrama;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private SharedPreferences sharedPreferences;
    private  TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("MyPreferences",
                getApplicationContext().MODE_PRIVATE);
        tts = new TextToSpeech(this,this);

        try {TimeUnit.SECONDS.sleep(5);}
        catch (Exception e) {}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isOnline()) {
                    TextView txInternet = (TextView) findViewById(R.id.txtConection);
                    txInternet.setText(R.string.errorconexion);
                    txInternet.setTextColor(Color.RED);

                    if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                        tts.stop();
                        speak("No tienes conexión a internet.");
                    }
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

    private void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text,
                    TextToSpeech.QUEUE_ADD, null, null);
        }
        else
        {
            tts.speak(text,
                    TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Override
    public void onInit(int i) {
        // configuracion del tts
        Log.d("Speech", "Set Language");
        tts.setLanguage(new Locale("es","ES"));
        tts.setPitch(1);
        tts.setSpeechRate(1);
    }
}
