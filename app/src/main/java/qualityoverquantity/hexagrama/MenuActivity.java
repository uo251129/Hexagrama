package qualityoverquantity.hexagrama;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import java.util.Locale;

import qualityoverquantity.hexagrama.util.State;

public class MenuActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private RadioButton rbDesactivate, rbActivate, rbNarrator, rbMusic;
    private SharedPreferences sharedPreferences;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rbDesactivate = findViewById(R.id.rbDesactivateInst);
        rbActivate = findViewById(R.id.rbActivateInst);
        rbNarrator = findViewById(R.id.rbNarrator);
        rbMusic = findViewById(R.id.rbMusic);
        sharedPreferences = getSharedPreferences("MyPreferences",
                getApplicationContext().MODE_PRIVATE);
        tts = new TextToSpeech(this,this);

        completeRadioButtons();
    }

    public void backCamera(View view) {
        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) speak("Volver atrás.");
        finish();
    }

    private void completeRadioButtons() {
        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
            Log.d("SharedPreferences", "Activando botón activar");
            rbActivate.setChecked(true);
        }
        else {
            Log.d("SharedPreferences", "Activando botón desactivar");
            rbDesactivate.setChecked(true);
        }

        if(sharedPreferences.getString("TIPO_SALIDA", "MUSICAL").equals("MUSICAL")) {
            Log.d("SharedPreferences", "Activando botón musica");
            rbMusic.setChecked(true);
        }
        else {
            Log.d("SharedPreferences", "Activando botón narrador");
            rbNarrator.setChecked(true);
        }
    }

    public void changeInitialInstructions(View view) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if(rbDesactivate.isChecked()) {
            Log.d("SharedPreferences","Se ha seleccionado desactivar instrucciones");
            editor.putBoolean("NARRADOR_PANTALLA", false);
        } else if (rbActivate.isChecked()) {
            Log.d("SharedPreferences","Se ha seleccionado activar instrucciones");
            editor.putBoolean("NARRADOR_PANTALLA", true);
        }

        editor.commit();
    }

    public void changeOutputType(View view) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if(rbNarrator.isChecked()) {
            Log.d("SharedPreferences","Se ha seleccionado tipo salida narrador");
            editor.putString("TIPO_SALIDA", "NARRADOR");
        } else if (rbMusic.isChecked()) {
            Log.d("SharedPreferences","Se ha seleccionado tipo salida musical");
            editor.putString("TIPO_SALIDA", "MUSICAL");
        }

        editor.commit();
    }

    @Override
    public void onInit(int i) {
        Log.d("Speech", "Set Language");
        tts.setLanguage(new Locale("es","ES"));
        tts.setPitch(1);
        tts.setSpeechRate(1);

        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true))
            speak(getResources().getString(R.string.menu_activity_speak));
    }

    private void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }
        else
        {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }
}
