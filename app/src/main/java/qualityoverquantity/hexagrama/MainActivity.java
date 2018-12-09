package qualityoverquantity.hexagrama;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import qualityoverquantity.hexagrama.util.RESTRequestSender;
import qualityoverquantity.hexagrama.util.State;
import qualityoverquantity.hexagrama.util.VolleyCallBack;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    public static final int GET_FROM_GALLERY = 1;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;

    TextureView textureView;
    CameraDevice cameraDevice;

    String cameraId;
    Size imageDimensions;
    Handler backgroundHandler;
    HandlerThread handlerThread;
    CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession cameraSession;

    private CaptureRequest mPreviewRequest;
    private  TextToSpeech tts;
    private SharedPreferences sharedPreferences;

    private RESTRequestSender restRequestSender;

    private	Intent	staveIntent;
    private Intent menuIntent;

    private ImageButton buttonCamera;
    private ProgressBar progressBar;
    private TextView txProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.texture);

        textureView.setSurfaceTextureListener(surfaceTextureListener);

        ImageButton button = (ImageButton)findViewById(R.id.uploadButton);
        button.setOnClickListener(uploadListener);

        buttonCamera = (ImageButton)findViewById(R.id.cameraButton);
        buttonCamera.setOnClickListener(cameraListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txProcessing = (TextView) findViewById(R.id.txtProcesando);

        sharedPreferences = getSharedPreferences("MyPreferences",
                getApplicationContext().MODE_PRIVATE);
        tts = new TextToSpeech(this,this);
        restRequestSender = RESTRequestSender.getInstance();
    }

    private View.OnClickListener uploadListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                tts.stop();
                speak("Cargar pentagrama.");
            }
            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
    };


    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {
            FileOutputStream outputPhoto = null;
            try {
                buttonCamera.setBackgroundColor(Color.GRAY);
                if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                    tts.stop();
                    speak("Capturar pentagrama. Se está procesando el pentagrama.");
                }
                Bitmap bitmap = textureView.getBitmap();
                Uri selectedImage = getImageUri(MainActivity.this, bitmap);

                staveIntent	=	new	Intent(MainActivity.this,StaveActivity.class);
                staveIntent.putExtra("staveImage",	selectedImage.toString());
                progressBar.setVisibility(View.VISIBLE);
                txProcessing.setVisibility(View.VISIBLE);

                //Request is sended before open next activity
                restRequestSender.sendRequest(MainActivity.this, bitmap,
                        new VolleyCallBack() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.INVISIBLE);
                                txProcessing.setVisibility(View.INVISIBLE);

                                if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                                    tts.stop();
                                    speak("Transformado correctamente.");
                                }

                                startActivity(staveIntent);
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.INVISIBLE);
                                txProcessing.setTextColor(Color.RED);
                                txProcessing.setText("No se ha podido transformar el pentagrama.");

                                txProcessing.postDelayed(new Runnable() {
                                    public void run() {
                                        txProcessing.setVisibility(View.INVISIBLE);
                                    }
                                }, 5000);

                                if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                                    tts.stop();
                                    speak("No se ha podido transformar el pentagrama.");
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outputPhoto != null) {
                        outputPhoto.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private File createImageFile(File galleryFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "image_" + timeStamp + "_";
        return File.createTempFile(imageFileName, ".jpg", galleryFolder);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                    tts.stop();
                    speak("Imagen cargada. Se está procesando el pentagrama.");
                }

                staveIntent	=	new	Intent(MainActivity.this,StaveActivity.class);
                staveIntent.putExtra("staveImage",	selectedImage.toString());
                progressBar.setVisibility(View.VISIBLE);
                txProcessing.setVisibility(View.VISIBLE);

                restRequestSender.sendRequest(MainActivity.this, bitmap,
                        new VolleyCallBack() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.INVISIBLE);
                                txProcessing.setVisibility(View.INVISIBLE);

                                if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                                    tts.stop();
                                    speak("Transformado correctamente.");
                                }

                                startActivity(staveIntent);
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.INVISIBLE);
                                txProcessing.setTextColor(Color.RED);
                                txProcessing.setText("No se ha podido transformar el pentagrama.");

                                txProcessing.postDelayed(new Runnable() {
                                    public void run() {
                                        txProcessing.setVisibility(View.INVISIBLE);
                                    }
                                }, 5000);

                                if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true)) {
                                    tts.stop();
                                    speak("No se ha podido transformar el pentagrama.");
                                }
                            }
                        });


            } catch (IOException i) {}
        }
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera(width, height);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void openCamera(int width, int height) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];

        CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        cameraManager.openCamera(cameraId, stateCallback, null);
    }

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                startCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void startCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(),imageDimensions.getHeight());

        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if(cameraDevice == null){ return; }

                cameraSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, null);

    }

    private void updatePreview() throws CameraAccessException {
        if (cameraDevice==null) { return; }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraSession.setRepeatingRequest(captureRequestBuilder.build(),null,backgroundHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();
        if(textureView.isAvailable()){
            try {
                openCamera(imageDimensions.getWidth(),imageDimensions.getHeight());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    private void startBackgroundThread() {
        handlerThread = new HandlerThread("Camera Background");
        handlerThread.start();

        backgroundHandler =  new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        try {
            tts.stop();
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    private void stopBackgroundThread() throws InterruptedException {
        handlerThread.quitSafely();
        handlerThread.join();

        backgroundHandler = null;
        handlerThread = null;
    }

    public void openMenu(View view) {
        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true))
            tts.stop();
            speak("Abrir menú de configuración");
        menuIntent	=	new	Intent(MainActivity.this,MenuActivity.class);
        startActivity(menuIntent);
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

        if(sharedPreferences.getBoolean("NARRADOR_PANTALLA",true))
            speak(getResources().getString(R.string.initial_instructions_speak));
    }

    @Override
    protected void onDestroy() {
        if (tts.isSpeaking()) tts.stop();
        super.onDestroy();
    }
}
