package edu.gmu.risp.fallcheck;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class FallSensorActivity extends MainActivity implements SensorEventListener {

    private SensorManager fallSensorManager;
    private float[] gravityVector = new float[3];
    private float[] linearAcceleration = new float[3];

    private static final float accelThreshold = 10.0f; // Set the fall value after testing

    private Button fallCheckStop;

    //private TextToSpeech textToSpeech;
    private TextView sensorDisplayData;

    private SharedPreferences sharedpreferences;

    private int androidAPILevel = android.os.Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_sensor);
        Activity activity = this;

        sensorDisplayData = activity.findViewById(R.id.FallCheckRunning);
        fallCheckStop = activity.findViewById(R.id.FallCheckStopButton);

        fallSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fallSensorManager.registerListener(this, fallSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        sharedpreferences = getSharedPreferences("FallCheckPreference", Context.MODE_PRIVATE);
        //textToSpeech = MainActivity.textSpeech;

        fallCheckStop.setOnClickListener(v -> finish());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float gravityLowPassFilter = 0.8f; // gravity

            gravityVector[0] = gravityLowPassFilter * gravityVector[0] + (1 - gravityLowPassFilter) * event.values[0];
            gravityVector[1] = gravityLowPassFilter * gravityVector[1] + (1 - gravityLowPassFilter) * event.values[1];
            gravityVector[2] = gravityLowPassFilter * gravityVector[2] + (1 - gravityLowPassFilter) * event.values[2];

            linearAcceleration[0] = event.values[0] - gravityVector[0];
            linearAcceleration[1] = event.values[1] - gravityVector[1];
            linearAcceleration[2] = event.values[2] - gravityVector[2];

            float acceleration = (float) Math.sqrt(linearAcceleration[0] * linearAcceleration[0]
                    + linearAcceleration[1] * linearAcceleration[1]
                    + linearAcceleration[2] * linearAcceleration[2]);

            // sensorDisplayData.setText(""+acceleration);
            if (acceleration > accelThreshold) {// ***** Add Check for Timer for false fall
                // Fall detected
                // Call alert system to notify user or emergency contacts
                sensorDisplayData.setText(R.string.FallOccured);
                speakText("Fall Detected. Contacting your preferred Emergency Contact.");
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String phone_number = sharedpreferences.getString("EmergencyContactNumber", "911");
                intent.setData(Uri.parse("tel:" + phone_number));
                startActivity(intent);
                // ***** Add Intent for Text Message. Get Location data before text

                finish();
            }else{
                if(sensorDisplayData!=null) {
                    sensorDisplayData.setText("Fall Check is Running");
                }
            }
        }
    }

    private void speakText(String txt) {
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int amStreamMusicMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
        am.setStreamVolume(am.STREAM_MUSIC, amStreamMusicMaxVol, 0);
        if (androidAPILevel < 21) {
            HashMap<String,String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
            MainActivity.textSpeech.speak(txt, TextToSpeech.QUEUE_FLUSH, params);
        } else { // android API level is 21 or higher...
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
            MainActivity.textSpeech.speak(txt, TextToSpeech.QUEUE_FLUSH, params, null);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}