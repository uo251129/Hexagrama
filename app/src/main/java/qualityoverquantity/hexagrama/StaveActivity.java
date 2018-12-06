package qualityoverquantity.hexagrama;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        //TESTING
        notes = new ArrayList<String>();
        notes.add("do");
        notes.add("re");
        notes.add("mi");
        notes.add("fa");
        notes.add("sol");
        notes.add("la");
        notes.add("si");
        notes.add("do");
        //
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

        playButton = (ImageButton) findViewById(R.id.playButton);
        isPlaying = false;
        sharedPreferences = getSharedPreferences("MyPreferences",
                getApplicationContext().MODE_PRIVATE);
        tts = new TextToSpeech(this,this);
        mediaPlayer = new MediaPlayer();
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
        if(!isPlaying) {
            isPlaying = true;
            playButton.setImageResource(R.drawable.pause);
            if (sharedPreferences.getString("TIPO_SALIDA", "MUSICAL").equals("MUSICAL"))
                playMusical();
            else
                playVoice();

            //Ends
            isPlaying = false;
            playButton.setImageResource(R.drawable.play);
        } else {
            isPlaying = false;
            playButton.setImageResource(R.drawable.play);
            tts.stop();
            mediaPlayer.stop();
        }
    }

    private void playMusical() {
        mediaPlayer.setLooping(false);
        for (String note: notes) {
            switch (note) {
                case ("do"):
                    Log.i("nota","Do");
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianoc);
                    break;
                case ("re"):
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianod);
                    Log.i("nota","Re");
                    break;
                case ("mi"):
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianoe);
                    Log.i("nota","Mi");
                    break;
                case ("fa"):
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianof);
                    Log.i("nota","Fa");
                    break;
                case ("sol"):
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianog);
                    Log.i("nota","Sol");
                    break;
                case ("la"):
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianoa);
                    Log.i("nota","La");
                    break;
                case ("si"):
                    mediaPlayer = MediaPlayer.create(this, R.raw.pianob);
                    Log.i("nota","Si");
                    break;
            }
            mediaPlayer.start();
            try {TimeUnit.MILLISECONDS.sleep(500);}
            catch (Exception e) {}
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
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
}
