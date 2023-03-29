package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Xử lý thông báo nhắc nhở tại đây
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("REMINDER_ALARM")) {
                String fileName = intent.getExtras().getString("fileName");
                if (fileName != null) {
                    Log.e(TAG, "onReceive: " + fileName);
                    Toast.makeText(context, fileName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}