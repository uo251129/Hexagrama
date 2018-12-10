package qualityoverquantity.hexagrama.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import qualityoverquantity.hexagrama.MainActivity;
import qualityoverquantity.hexagrama.R;
import qualityoverquantity.hexagrama.StaveActivity;

public class ReproductionTask extends AsyncTask<String,String,String> {

    private StaveActivity activity;
    private MediaPlayer mediaPlayer;
    private ArrayList<String> notes;

    public ReproductionTask(StaveActivity activity, MediaPlayer mediaPlayer, ArrayList<String> notes) {
        this.activity = activity;
        this.mediaPlayer = mediaPlayer;
        this.notes = notes;
    }

    @Override
    protected String doInBackground(String... strings) {
        List<MediaPlayer> mps = new ArrayList<MediaPlayer>();
        for (String note: notes) {
            mps.add(reproduceNoteMusic(note));
        }

        for (MediaPlayer mp : mps) {
            if (!activity.isPaused()) {
                mp.start();
                try {TimeUnit.MILLISECONDS.sleep(1000);}
                catch (Exception e) {}
                mp.reset();
            }
        }

        return null;
    }

    private MediaPlayer reproduceNoteMusic(String note) {
        MediaPlayer mediaPlayerAux = new MediaPlayer();
        switch (note) {
            case ("C"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianoc);
                break;
            case ("D"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianod);
                break;
            case ("E"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianoe);
                break;
            case ("F"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianof);
                break;
            case ("G"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianog);
                break;
            case ("A"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianoa);
                break;
            case ("B"):
                mediaPlayerAux = MediaPlayer.create(activity, R.raw.pianob);
                break;
        }

        return mediaPlayerAux;
    }
}
