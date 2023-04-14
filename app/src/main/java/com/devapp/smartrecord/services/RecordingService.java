package com.devapp.smartrecord.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.RecordActivity;

import java.util.Locale;

public class RecordingService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private long pausedTime = 0;
    private int elapsedTime = 0;
    private final Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private final Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (pausedTime > 0) {
                elapsedTime = (int) pausedTime;
                pausedTime = 0;
            } else {
                elapsedTime++;
            }
            builder.setSubText(getFormattedTime(elapsedTime));
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(
                "recording_channel_id",
                "Recording Channel",
                NotificationManager.IMPORTANCE_HIGH);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private String getFormattedTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null && action.equals("PAUSE_RECORDING")) {
            setTimePause();
        } else if (action != null && action.equals("RESUME_RECORDING")) {
            setTimeResume();
        }
          else if (action != null && action.equals("STOP_RECORDING")) {
            stopForeground(true);
            notificationManager.cancel(NOTIFICATION_ID);
            handler.removeCallbacks(updateNotificationRunnable);
        }
        else{
            showNotification();
        }
        return START_STICKY;
    }

    private void setTimePause()
    {
        pausedTime = elapsedTime;
        handler.removeCallbacksAndMessages(null);
    }

    private void setTimeResume(){
        startForeground(1, builder.build());
        handler.postDelayed(updateNotificationRunnable, 0);
    }

    private void showNotification(){
        builder = new NotificationCompat.Builder(this, "recording_channel_id")
                .setContentTitle("Recording in progress")
                .setContentText("Tap to stop recording")
                .setSmallIcon(R.drawable.ic_play_record)
                .setOngoing(true)
                .setVibrate(null)
                .setSound(null)
                .setOnlyAlertOnce(true)
                .setContentIntent(getStopRecordingIntent())
                .setSubText(getFormattedTime(elapsedTime));

        startForeground(NOTIFICATION_ID, builder.build());

        handler.postDelayed(updateNotificationRunnable, 1000);
    }

    private PendingIntent getStopRecordingIntent() {
        elapsedTime = 0;
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
