package com.devapp.smartrecord.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;


import com.devapp.smartrecord.ui.alarm.HandleDataAlarm;

import java.io.IOException;

public class AlarmNotifyService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String fileName = intent.getExtras().getString("path");
        String time = intent.getExtras().getString("time");
        // Tạo một đối tượng MediaPlayer để phát âm thanh báo thức
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(111);
                }
            });
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Trả về START_STICKY để service có thể bị hủy và được khởi động lại bởi hệ thống
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Dừng phát âm thanh báo thức và giải phóng tài nguyên
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}