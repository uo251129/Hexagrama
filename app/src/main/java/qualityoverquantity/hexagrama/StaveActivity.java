package qualityoverquantity.hexagrama;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.midi.MidiManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import qualityoverquantity.hexagrama.util.RESTRequestSender;
import qualityoverquantity.hexagrama.util.ReproductionTask;

public class StaveActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private Intent parameters;
    private Bitmap staveImage;
    private ArrayList<String> notes;
    private SharedPreferences sharedPreferences;
    private  TextToSpeech tts;
    private Intent menuIntent;
    private ImageButton playButton;
    private Boolean isPlaying;
    private MediaPlayer mediaPlayer;

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

        RESTRequestSender restRequestSender = RESTRequestSender.getInstance();
        this.notes = restRequestSender.notes;

        imageView.setImageBitmap(staveImage);

        ImageButton button = (ImageButton)findViewById(R.id.backButton);
        button.setOnClickListener(backListener);

        playButton = (ImageButton) findViewById(R.id.playButton);
        isPlaying = false;
        sharedPreferences = getSharedPreferences("MyPreferences",
                getApplicationContext().MODE_PRIVATE);
        tts = new TextToSpeech(this,this);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.prepare();
        } catch (Exception e) {}
    }

    private View.OnClickListener backListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                tts.stop();
                speak("Volver atrás.");
            }
            finish();
        }
    };

    public void openMenu(View view) {
        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
            tts.stop();
            speak("Abrir menú de configuración.");
        }
        menuIntent	=	new	Intent(StaveActivity.this,MenuActivity.class);
        startActivity(menuIntent);
    }

    public void playStave(View view) {
        tts.stop();

        if(isPlaying) {
            isPlaying = false;
            playButton.setImageResource(R.drawable.play);
        } else if (sharedPreferences.getString("TIPO_SALIDA", "MUSICAL").equals("MUSICAL")) {
            playMusical();
        }  else {
            playVoice();
        }
    }

    private void playMusical() {
        isPlaying = true;
        playButton.setImageResource(R.drawable.stop);
        new ReproductionTask(this,mediaPlayer,notes).execute("1","2","3");
    }



    private  void playVoice() {
        isPlaying = true;
        playButton.setImageResource(R.drawable.stop);
        for(String note: notes)
            speak(convertToLatinNote(note));
        playButton.setImageResource(R.drawable.play);
    }

    private String convertToLatinNote(String note) {
        switch (note) {
            case ("C"):
                return "Do";
            case ("D"):
                return "Re";
            case ("E"):
                return "Mi";
            case ("F"):
                return "Fa";
            case ("G"):
                return "Sol";
            case ("A"):
                return "La";
            case ("B"):
                return "Si";
        }

        return "";
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

    @Override
    protected void onDestroy() {
        mediaPlayer.stop();
        if (tts.isSpeaking()) tts.stop();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mediaPlayer.reset();
        if (tts.isSpeaking()) tts.stop();
        super.onPause();
    }

    public boolean isPaused() {
        return !this.isPlaying;
    }
}
