package qualityoverquantity.hexagrama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import qualityoverquantity.hexagrama.util.State;

public class MenuActivity extends AppCompatActivity {

    private RadioButton rbDesactivate, rbActivate, rbNarrator, rbMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rbDesactivate = findViewById(R.id.rbDesactivateInst);
        rbActivate = findViewById(R.id.rbActivateInst);
        rbNarrator = findViewById(R.id.rbNarrator);
        rbMusic = findViewById(R.id.rbMusic);
    }

    public void backCamera(View view) {
        finish();
    }

    public void changeInitialInstructions(View view) {
        if(rbDesactivate.isActivated()) {
            State.INSTRUCCIONES_INICIO = false;
        } else if (rbActivate.isActivated()) {
            State.INSTRUCCIONES_INICIO = true;
        }
    }

    public void changeOutputType(View view) {
        if(rbNarrator.isActivated()) {
            State.TIPO_SALIDA = State.NARRADOR;
        } else if (rbMusic.isActivated()) {
            State.TIPO_SALIDA = State.MUSICAL;
        }
    }
}
