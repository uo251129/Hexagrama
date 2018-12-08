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
        //notes = parameters.getStringArrayListExtra("notes");
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
        playButton.setImageResource(R.drawable.stop);

        if (sharedPreferences.getString("TIPO_SALIDA", "MUSICAL").equals("MUSICAL")) {
            playMusical();
            //playButton.setImageResource(R.drawable.play);
        }  else {
            isPlaying = true;
            playVoice();
            //playButton.setImageResource(R.drawable.play);
        }

        //Ends
        isPlaying = false;
    }

    public void stopStave(View view) {

    }

    private void playMusical() {
        List<MediaPlayer> mps = new ArrayList<MediaPlayer>();
        for (String note: notes) {
            mps.add(reproduceNoteMusic(note));
        }
        for (MediaPlayer mp : mps) {
            mp.start();
            try {TimeUnit.MILLISECONDS.sleep(1000);}
            catch (Exception e) {}
            mp.reset();
        }
    }

    private MediaPlayer reproduceNoteMusic(String note) {
        MediaPlayer mediaPlayerAux = new MediaPlayer();
        switch (note) {
            case ("C"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianoc);
                break;
            case ("D"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianod);
                break;
            case ("E"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianoe);
                break;
            case ("F"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianof);
                break;
            case ("G"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianog);
                break;
            case ("A"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianoa);
                break;
            case ("B"):
                mediaPlayerAux = MediaPlayer.create(this, R.raw.pianob);
                break;
        }

        return mediaPlayerAux;
    }

    private  void playVoice() {
        for(String note: notes)
            speak(note);
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
}
