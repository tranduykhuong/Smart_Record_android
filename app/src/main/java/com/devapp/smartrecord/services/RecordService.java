package com.devapp.smartrecord.services;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.devapp.smartrecord.ConfigurationClass;
import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.RecordActivity1;
import com.suman.voice.graphviewlibrary.WaveSample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordService extends Service {
    private MediaRecorder mediaRecorder;

    private long startTime = 0;
    private String finalName;
    private String fileName;
    private volatile Boolean stop = false;
    private Boolean isPaused = false;
    private String fileExt;
    private File file;

    private static final int NOTIFICATION_ID = 201;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private int elapsedTime = 0;
    private final Handler handler = new Handler();
    private final List<WaveSample> pointList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        ConfigurationClass config = new ConfigurationClass(getApplicationContext());
        config.getConfig();
        fileExt = "." + config.getFileFormat();
        elapsedTime = 0;

        getLocation();

        NotificationChannel channel = new NotificationChannel(
                "recording_channel_id",
                "Recording Channel",
                NotificationManager.IMPORTANCE_HIGH);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        showNotification();

        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter("record-event"));

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isRecording", true);
        editor.apply();

        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("ACTION_RECORD");
            if (action != null && action.equals("PAUSE_RECORDING")) {
                if (!isPaused) {
                    pauseRecording();
                    setTimePause();
                }
            }
            else if (action != null && action.equals("RESUME_RECORDING")) {
                if (isPaused) {
                    resumeRecording();
                    setTimeResume();
                }
            }
            else if (action != null && action.equals("SAVE_RECORDING")) {
                stopRecording();
                saveRecord();
                stopForeground(true);
                notificationManager.cancel(NOTIFICATION_ID);
                handler.removeCallbacks(updateNotificationRunnable);
                handler.removeCallbacks(updateGraphView);
            }
            else if (action != null && action.equals("NO_SAVE_RECORDING")) {
                stopRecording();
                stopWithNoSave();
                stopForeground(true);
                notificationManager.cancel(NOTIFICATION_ID);
                handler.removeCallbacks(updateNotificationRunnable);
                handler.removeCallbacks(updateGraphView);
            }
        }
    };

    public void startRecording() {
        this.stop = false;
        this.isPaused = false;

        if(fileName == null)
        {
            fileName = "Record";
            finalName = fileName + fileExt;
        }

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName + fileExt);
        if(file.exists()) {
            int i = 1;
            while (file.exists())
            {
                finalName = fileName + " (" + i + ")" + fileExt;
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
        mediaRecorder.setAudioEncodingBitRate(192000);
        mediaRecorder.setAudioSamplingRate(48000);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            pointList.clear();
            startTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseRecording(){
        if (mediaRecorder != null) {
            mediaRecorder.pause();
            isPaused = true;
        }
    }
    public void resumeRecording(){
        if (mediaRecorder != null) {
            mediaRecorder.resume();
            isPaused = false;
        }
    }

    public void stopRecording() {
        this.stop = true;

        if(mediaRecorder != null)
        {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isRecording", false);
        editor.apply();

        onDestroy();
    }

    private void saveRecord(){
        Toast.makeText(getApplicationContext(), this.getString(R.string.save_file) + finalName, Toast.LENGTH_LONG).show();
    }

    public void stopWithNoSave(){
        File fileDel = new File(String.valueOf(file.getAbsoluteFile()));
        if (fileDel.exists()) {
            fileDel.delete();
            Toast.makeText(getApplicationContext(), this.getString(R.string.remove_file), Toast.LENGTH_LONG).show();
        }
    }

    private final Runnable updateGraphView = new Runnable() {
        @Override
        public void run() {
            int ampli = mediaRecorder.getMaxAmplitude();
            pointList.add(new WaveSample(System.currentTimeMillis() - startTime, ampli));

            Intent intent = new Intent("record-activity-event");
            intent.putExtra("GRAPH_COOR", elapsedTime * 1000);
            intent.putExtra("GRAPH_VALUE", ampli);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            handler.postDelayed(this, 150);
        }
    };

    private final Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime++;

            Intent intent = new Intent("record-activity-event");
            intent.putExtra("CURRENT_TIME", elapsedTime);
            intent.putExtra("FILE_NAME", finalName);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            builder.setSubText(getFormattedTime(elapsedTime));
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            handler.postDelayed(this, 1000);
        }
    };

    private void showNotification(){
        Intent intent = new Intent(this, RecordActivity1.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        builder = new NotificationCompat.Builder(this, "recording_channel_id")
                .setContentTitle(this.getString(R.string.broadcast_title))
//                .setContentText(this.getString(R.string.broadcast_action))
                .setContentText(finalName)
                .setSmallIcon(R.drawable.ic_play_record)
                .setOngoing(true)
                .setVibrate(null)
                .setSound(null)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setSubText(getFormattedTime(elapsedTime));

        startForeground(NOTIFICATION_ID, builder.build());

        handler.postDelayed(updateNotificationRunnable, 1000);
        handler.postDelayed(updateGraphView, 150);
    }

    private void setTimePause()
    {
        handler.removeCallbacks(updateNotificationRunnable);
        handler.removeCallbacks(updateGraphView);
    }

    private void setTimeResume()
    {
        startForeground(NOTIFICATION_ID, builder.build());
        handler.postDelayed(updateNotificationRunnable, 1000);
        handler.postDelayed(updateGraphView, 150);
    }

    private String getFormattedTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds);
    }

    private void getLocation(){
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
                fileName = finalAddress;
            }
        } catch (IOException e) {
            fileName = "Record";
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(mediaRecorder != null)
            stopRecording();

        handler.removeCallbacks(updateNotificationRunnable);
        handler.removeCallbacks(updateGraphView);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        stopForeground(true);
        stopSelf();

        super.onDestroy();
    }
}
