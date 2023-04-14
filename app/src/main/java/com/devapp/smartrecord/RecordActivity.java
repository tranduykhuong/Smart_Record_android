package com.devapp.smartrecord;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.devapp.smartrecord.services.RecordingService;
import com.devapp.smartrecord.services.RecordingActivity;
import com.devapp.smartrecord.services.RecordingActivity;
import com.suman.voice.graphviewlibrary.GraphView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecordActivity extends AppCompatActivity {
    private boolean flagRecording = false;
    long timeWhenPaused = 0;
    private ImageButton btnPlay;
    private Chronometer chrnmterTime;
    private TextView txtRecordName;
    private List samples;
    private GraphView graphView;
    private RecordingActivity recorder;
    private TelephonyManager telephonyManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageButton btnStop = findViewById(R.id.record_btn_stop);
        btnPlay = findViewById(R.id.record_btn_play);
        chrnmterTime = findViewById(R.id.record_time_recording);
        txtRecordName = findViewById(R.id.record_name);
        graphView = findViewById(R.id.graphView);
        graphView.setGraphColor(Color.rgb(18, 17, 17));
        graphView.setCanvasColor(Color.rgb(255, 255, 255));
        graphView.setTimeColor(Color.rgb(0, 0, 0));
        graphView.setWaveLengthPX(13);

        recorder = RecordingActivity.getInstance();

        getLocation();

        ConfigurationClass config = new ConfigurationClass(getApplicationContext());
        config.getConfig();
        recorder.setFileExt("." + config.getFileFormat());
        Log.e(TAG, "onCreate: " +"." + config.getFileFormat());

        startRecord();

        btnPlay.setOnClickListener(view -> recordAudio());

        btnStop.setOnClickListener(view -> {
            flagRecording = true;
            btnPlay.setImageResource(R.drawable.ic_play_record);
            chrnmterTime.setBase(SystemClock.elapsedRealtime());
            chrnmterTime.stop();
            confirmDelete();
        });

    }
    private void deleteRecord(){
        recorder.stopWithNoSave();
        Toast.makeText(getApplicationContext(), "File is deleted", Toast.LENGTH_LONG).show();

        Intent intentInform = new Intent(this, RecordingActivity.class);
        intentInform.setAction("STOP_RECORDING");
        startService(intentInform);

        Intent returnHome = new Intent(this, HomeActivity.class);
        startActivity(returnHome);
    }
    private void saveRecord(){
        recorder.stopRecording();
        Toast.makeText(getApplicationContext(), "Saved_" + recorder.getOutputFilePath(), Toast.LENGTH_LONG).show();

        Intent intentInform = new Intent(this, RecordingActivity.class);
        intentInform.setAction("STOP_RECORDING");
        startService(intentInform);

        Intent returnHome = new Intent(this, HomeActivity.class);
        startActivity(returnHome);
    }
    private void confirmDelete(){
        AlertDialog.Builder alertDiaglog = new AlertDialog.Builder(this);
        alertDiaglog.setTitle("Lưu bản ghi");
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage("Bạn có muốn lưu bản ghi?");
        alertDiaglog.setPositiveButton("Lưu", (dialogInterface, i) -> saveRecord());
        alertDiaglog.setNegativeButton("Xóa", (dialogInterface, i) -> deleteRecord());

        alertDiaglog.show();
    }

    @SuppressLint("SetTextI18n")
    public void getLocation(){
        //GET LOCATION
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        double latitude = 0, longitude = 0;

        if(location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                String finalAddress = addressString.substring(0, addressString.indexOf(','));
                finalAddress = finalAddress.replace("/", "-");
                txtRecordName.setText(finalAddress);
                recorder.setOutputFilePath(finalAddress);
            }
        } catch (IOException e) {
            txtRecordName.setText("Record");
            recorder.setOutputFilePath("Record");
            e.printStackTrace();
        }
    }

    public void recordAudio(){
        onRecord(flagRecording);
        flagRecording = !flagRecording;
    }

    private void startRecord(){
        Toast.makeText(getApplicationContext(), "Record...", Toast.LENGTH_SHORT).show();
        btnPlay.setImageResource(R.drawable.ic_pause_record);
        File folder = new File(Environment.getExternalStorageDirectory() + "/Recordings");

        if(!folder.exists())
        {
            folder.mkdir();
        }
        chrnmterTime.setBase(SystemClock.elapsedRealtime());
        chrnmterTime.start();

        Intent intentInform = new Intent(this, RecordingActivity.class);
        startForegroundService(intentInform);

        recorder.startRecording();
        recorder.startPlotting(graphView);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void onRecord(boolean start) {
        if(start)
        {
            ResumeRecord();
        }
        else
        {
            PauseRecord();
        }
    }

    public void ResumeRecord(){
        Toast.makeText(this, "Resume", Toast.LENGTH_LONG).show();
        btnPlay.setImageResource(R.drawable.ic_pause_record);

        chrnmterTime.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
        chrnmterTime.start();
        recorder.resumeRecording();

        Intent intentInform = new Intent(this, RecordingActivity.class);
        intentInform.setAction("RESUME_RECORDING");
        startService(intentInform);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void PauseRecord()
    {
        Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show();
        btnPlay.setImageResource(R.drawable.ic_play_record);

        recorder.pauseRecording();

        samples = recorder.getSamples();
        graphView.showFullGraph(samples);

        timeWhenPaused = SystemClock.elapsedRealtime() - chrnmterTime.getBase();
        chrnmterTime.stop();

        Intent intentInform = new Intent(this, RecordingActivity.class);
        intentInform.setAction("PAUSE_RECORDING");
        startService(intentInform);
    }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (state == TelephonyManager.CALL_STATE_RINGING) {
                PauseRecord();

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {

            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };
    protected void onResume() {
        super.onResume();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        Toast.makeText(getApplicationContext(), "Đang dừng nè má", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(getApplicationContext(), "Đang hủy nè má", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    public void changeLayoutFromRecord(View view){
        switch (view.getId()) {
            case R.id.record_btn_back: {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isRecording", flagRecording);
                editor.apply();
                finish();
                break;
            }
        }
    }
}