package edu.gmu.risp.fallcheck;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;

public class FallSensorActivity extends MainActivity implements SensorEventListener {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Double latitude = Double.valueOf(00000);
    private Double longitude = Double.valueOf(11111);
    private boolean cnt = false;
    private boolean f_trigger = false;

    private boolean time_out = false;
    private SensorManager fallSensorManager;
    private float[] gravityVector = new float[3];
    private float[] linearAcceleration = new float[3];

    private float accelThreshold = 14.0f; // Set the fall value after testing

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
        accelThreshold = 14.0f;


        sensorDisplayData = activity.findViewById(R.id.FallCheckRunning);
        fallCheckStop = activity.findViewById(R.id.FallCheckStopButton);

        fallSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fallSensorManager.registerListener(this, fallSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        sharedpreferences = getSharedPreferences("FallCheckPreference", Context.MODE_PRIVATE);
        //textToSpeech = MainActivity.textSpeech;
        sensorDisplayData.setText("Fall Check is Running");
        sensorDisplayData.setTextSize(20);
        cnt = false;
        f_trigger = false;
        time_out = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Do something with the location
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        fallCheckStop.setOnClickListener(v -> stop_call());
    }

    private void stop_call() {
        if(f_trigger == false) {
            cnt = false;
            time_out = false;
            accelThreshold = 30.0f;
            finish();

        }
        cnt = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float gravityLowPassFilter = 0.7f; // gravity

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
                if (f_trigger == false) {
                    cnt = true;
                    f_trigger = true;
                    speakText("Fall Detected. Contacting your preferred Emergency Contact.");
                    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    activityManager.moveTaskToFront(this.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locationListener);

                    CountDownTimer time_d = new CountDownTimer(10000, 100) { // 30 seconds countdown with 1 second interval
                        public void onTick(long millisUntilFinished) {
                            // Called every second during the countdown
                            sensorDisplayData.setText("Fall detected if safe press stop within " + millisUntilFinished / 1000);
                            sensorDisplayData.setTextSize(30);
                            if(cnt == false){
                                f_trigger = false;
                                sensorDisplayData.setText("Fall Check is Running");
                                sensorDisplayData.setTextSize(20);
                                this.cancel();
                                accelThreshold = 30.0f;
                                finish();

                            }
                        }

                        public void onFinish() {
                            if (cnt == true) {
                                String phone_number = sharedpreferences.getString("EmergencyContactNumber", "911");
                                String sms_msg = "Please check this" + " https://maps.google.com/?q=" + latitude + "," + longitude + "";

                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phone_number, null, sms_msg, null, null);
                                Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.putExtra("simSlot", 0);
                                intent.setData(Uri.parse("tel:" + phone_number));
                                startActivity(intent);
                                f_trigger = false;
                                cnt = false;
                                accelThreshold = 30.0f;
                                finish();
                            }
                            sensorDisplayData.setText("Fall Check is Running");
                            sensorDisplayData.setTextSize(20);
                            accelThreshold = 30.0f;
                            finish();
                        }
                    };
                    time_d.start();

                } else {
                    if (sensorDisplayData != null) {
                        //sensorDisplayData.setText("Fall Check is Running");
                    }
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