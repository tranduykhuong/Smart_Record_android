package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.services.AlarmNotifyService;

import java.io.File;
import java.io.IOException;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "REMINDER_CHANNEL_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Xử lý thông báo nhắc nhở tại đây
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("REMINDER_ALARM")) {
                String fileName = intent.getExtras().getString("fileName");
                if (fileName != null) {
                    Log.e(TAG, "onReceive: " + fileName);
                    Toast.makeText(context, fileName, Toast.LENGTH_SHORT).show();

                    Intent serviceIntent = new Intent(context, AlarmNotifyService.class);
                    context.startService(serviceIntent);

                    // Hiển thị notification
                    Intent intentRemoveNotify = new Intent(context, RemoveNotifyReceiver.class);
                    PendingIntent pendingIntentRemove = PendingIntent.getBroadcast(context, 0, intentRemoveNotify, PendingIntent.FLAG_MUTABLE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "My channel";
                        String description = "My channel description";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("REMINDER_CHANNEL_ID", name, importance);
                        channel.setDescription(description);
                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_alarm_clock)
                            .setContentTitle("SMART RECORD - REMINDER")
                            .setContentText(fileName)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .addAction(R.drawable.ic_pause_record, "Tắt ngay", pendingIntentRemove)
                            .setDeleteIntent(pendingIntentRemove)
                            .setContentIntent(pendingIntentRemove);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    notificationManager.notify(111, builder.build());
                }
            }
        }
    }
}