package com.devapp.smartrecord.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RecordingService extends Service {

    MediaRecorder mediaRecorder;
    long mStartingTimeMillis = 0;

    File file;
    String fileName;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null && action.equals("PAUSE_RECORDING"))
        {
            pauseRecording();
        }
        else if(action != null && action.equals("RESUME_RECORDING"))
        {
            resumeRecording();
        }
        else if(action != null && action.equals("DELETE_RECORDING"))
        {
            stopWithNoSave();
        }
        else
        {
            startRecording();
            Toast.makeText(getApplicationContext(), "Ghi...", Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }
    private void startRecording() {
        //Lấy địa điểm:
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
                fileName = finalAddress;
            }
            else{
                fileName = "Record";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String fileExt = ".mp3";

        //fileName = "audio_" + ts;

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName + fileExt);
        if(file.exists()) {
            int i = 1;
            while (file.exists())
            {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName + " (" + i + ")" + fileExt);
                i++;
            }
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    private void pauseRecording()
    {
        if (mediaRecorder != null) {
            mediaRecorder.pause();
            Toast.makeText(getApplicationContext(), "Tạm dừng", Toast.LENGTH_LONG).show();
        }
    }
    private void resumeRecording(){
        if (mediaRecorder != null) {
            Toast.makeText(getApplicationContext(), "Tiếp tục", Toast.LENGTH_LONG).show();
            mediaRecorder.resume();
        }
    }
    private void stopRecording(){
        if (mediaRecorder != null) {
            Toast.makeText(getApplicationContext(), "Đã lưu_" + file.getAbsoluteFile(), Toast.LENGTH_LONG).show();
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }
    private void stopWithNoSave(){
        mediaRecorder.stop();
        mediaRecorder.release();

        Toast.makeText(getApplicationContext(), "Đã xóa", Toast.LENGTH_LONG).show();

        File fileDel = new File(String.valueOf(file.getAbsoluteFile()));
        if (fileDel.exists()) {
            fileDel.delete();
        }
    }

    @Override
    public void onDestroy() {
        if(mediaRecorder != null)
        {
            stopRecording();
            Toast.makeText(getApplicationContext(), "Đã lưu_" + file.getAbsoluteFile(), Toast.LENGTH_LONG).show();
        }
        super.onDestroy();
    }
}
