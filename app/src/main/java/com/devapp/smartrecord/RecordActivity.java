package com.devapp.smartrecord;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.devapp.smartrecord.services.RecordingService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RecordActivity extends AppCompatActivity {
    private boolean flagBtn = false; //Đang ghi
    long timeWhenPaused = 0;
    private ImageButton btnStop, btnPlay;
    private Chronometer chrnmterTime;
    private TextView txtRecordName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_screen);
        getSupportActionBar().hide();

        btnStop = (ImageButton) findViewById(R.id.record_btn_stop);
        btnPlay = (ImageButton) findViewById(R.id.record_btn_play);
        chrnmterTime = (Chronometer) findViewById(R.id.record_time_recording);
        txtRecordName = (TextView) findViewById(R.id.record_name);

        getLocation();

        startRecord();

        btnPlay.setOnClickListener(view -> {
            recordAudio();
        });

        btnStop.setOnClickListener(view -> {
            flagBtn = true;
            btnPlay.setImageResource(R.drawable.ic_play_record);
            chrnmterTime.setBase(SystemClock.elapsedRealtime());
            chrnmterTime.stop();
            confirmDelete();
        });

    }
    private void deleteRecord(){
        Intent intentRecord = new Intent(this, RecordingService.class);
        intentRecord.setAction("DELETE_RECORDING");
        startService(intentRecord);
        Intent returnHome = new Intent(this, HomeActivity.class);
        startActivity(returnHome);
    }
    private void saveRecord(){
        Intent intentRecord = new Intent(this, RecordingService.class);
        stopService(intentRecord);
        Intent returnHome = new Intent(this, HomeActivity.class);
        startActivity(returnHome);
    }
    private void confirmDelete(){
        AlertDialog.Builder alertDiaglog = new AlertDialog.Builder(this);
        alertDiaglog.setTitle("Lưu bản ghi");
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage("Bạn có muốn lưu bản ghi?");
        alertDiaglog.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveRecord();
            }
        });
        alertDiaglog.setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRecord();
            }
        });

        alertDiaglog.show();
    }

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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Sử dụng Geocoder để lấy địa chỉ dựa trên vị trí hiện tại
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                String finalAddress = addressString.substring(0, addressString.indexOf(','));
                txtRecordName.setText(finalAddress);
            }
        } catch (IOException e) {
            txtRecordName.setText("Record");
            e.printStackTrace();
        }
    }

    public void recordAudio(){
        onRecord(flagBtn);
        flagBtn = !flagBtn;
    }

    private void startRecord(){
        btnPlay.setImageResource(R.drawable.ic_pause_record);
        File folder = new File(Environment.getExternalStorageDirectory() + "/MySoundRec");

        if(!folder.exists())
        {
            folder.mkdir();
        }
        chrnmterTime.setBase(SystemClock.elapsedRealtime());
        chrnmterTime.start();

        Intent intentRecord = new Intent(this, RecordingService.class);
        startService(intentRecord);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void onRecord(boolean start) {
        if(start)
        {
            btnPlay.setImageResource(R.drawable.ic_pause_record);

            chrnmterTime.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
            chrnmterTime.start();

            Intent intentRecord = new Intent(this, RecordingService.class);
            intentRecord.setAction("RESUME_RECORDING");
            startService(intentRecord);

            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else
        {
            btnPlay.setImageResource(R.drawable.ic_play_record);

            Intent intentRecord = new Intent(this, RecordingService.class);
            intentRecord.setAction("PAUSE_RECORDING");
            startService(intentRecord);

            timeWhenPaused = SystemClock.elapsedRealtime() - chrnmterTime.getBase();
            chrnmterTime.stop();
        }
    }

    public void changeLayoutFromRecord(View view){
        switch (view.getId()) {
            case R.id.record_btn_back: {
                Intent intent = new Intent(this, ReplayActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
