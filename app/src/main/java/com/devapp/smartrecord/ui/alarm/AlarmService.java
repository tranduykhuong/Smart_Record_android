package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.devapp.smartrecord.R;

import java.io.IOException;

public class AlarmService extends Service {
    private static final String CHANNEL_ID = "AlarmServiceChannel";
    private Intent alarmIntent;
    private PendingIntent pendingIntent;
    private long milliseconds = 0;
    private AlarmManager alarmManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (intent == null) {
            Log.e(TAG, "actionAlarm: null");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_id",
                    "My Channel",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(getApplicationContext(), "my_channel_id")
                    .setSmallIcon(R.drawable.ic_alarm_clock)
                    .setContentTitle("Smart Record - Reminder")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build();
            startForeground(112, notification);
        } else {
            startForeground(112, new Notification());
        }

        // Lấy ra thông tin về thời gian nhắc nhở từ Intent
        milliseconds = intent.getLongExtra("timeInMillis", 0);
        String path = intent.getStringExtra("path");
        String time = intent.getStringExtra("time");
        long oldID = intent.getLongExtra("oldID", 0);

        alarmIntent = new Intent(this, ReminderReceiver.class);
        alarmIntent.setAction("REMINDER_ALARM");
        alarmIntent.putExtra("path", path);
        alarmIntent.putExtra("time", time);

        if (oldID != 0) {
            Log.e(TAG, "oldID: " + oldID);
            pendingIntent = PendingIntent.getBroadcast(this, (int) oldID, alarmIntent, PendingIntent.FLAG_MUTABLE);
            alarmManager.cancel(pendingIntent);
        }
        pendingIntent = PendingIntent.getBroadcast(this, (int) milliseconds, alarmIntent, PendingIntent.FLAG_MUTABLE);

//        long timeInMillis = System.currentTimeMillis() + 7000 + (int) (Math.random()*10);
        long timeInMillis = milliseconds;
        Log.e(TAG, "onStartCommand: " + timeInMillis);
        // Đặt lịch nhắc nhở bằng AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: service");
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        stopForeground(true);
        stopSelf();
    }
}