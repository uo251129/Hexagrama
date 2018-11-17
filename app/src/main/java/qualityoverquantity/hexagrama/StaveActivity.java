package qualityoverquantity.hexagrama;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;

public class StaveActivity extends AppCompatActivity {
    private Intent parameters;
    private Bitmap staveImage;

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
    }

    private View.OnClickListener backListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
}
