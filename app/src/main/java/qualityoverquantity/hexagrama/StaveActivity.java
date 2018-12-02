package qualityoverquantity.hexagrama;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Locale;

public class StaveActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private Intent parameters;
    private Bitmap staveImage;
    private SharedPreferences sharedPreferences;
    private  TextToSpeech tts;
    private Intent menuIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stave);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        parameters = getIntent();
        Uri selectedImage = Uri.parse(parameters.getStringExtra("staveImage"));
        try {
            staveImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);
        //staveImage = Bitmap.createScaledBitmap(staveImage,size.x, size.y, true);
        imageView.setImageBitmap(staveImage);

        ImageButton button = (ImageButton)findViewById(R.id.backButton);
        button.setOnClickListener(backListener);

        sharedPreferences = getSharedPreferences("MyPreferences",
                getApplicationContext().MODE_PRIVATE);
        tts = new TextToSpeech(this,this);
    }

    private View.OnClickListener backListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true))
                speak("Volver atrás.");
            finish();
        }
    };

    public void openMenu(View view) {
        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true))
            speak("Abrir menú de configuración.");
        menuIntent	=	new	Intent(StaveActivity.this,MenuActivity.class);
        startActivity(menuIntent);
    }

    @Override
    public void onInit(int i) {
        Log.d("Speech", "Set Language");
        tts.setLanguage(new Locale("es","ES"));
        tts.setPitch(1);
        tts.setSpeechRate(1);

        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true))
            speak(getResources().getString(R.string.stave_activity_speak));
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
