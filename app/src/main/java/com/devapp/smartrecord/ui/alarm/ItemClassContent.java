package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class ItemClassContent {
    private String title;
    private String path;
    private String time = "";
    private boolean checked;

    private long milliseconds = 0;

    private Context context;
    private Intent serviceIntent;

    public ItemClassContent(Context context, String path, String time, boolean checked) {
        this.path = path;
        this.time = time;
        this.checked = checked;
        this.context = context;

        String title[] = path.split("/");
        this.title = title[title.length - 1];

        actionAlarm();
    }

    public String getTitle() {
        return title;
    }
    public String getPath() {
        return path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        context.stopService(serviceIntent);
        actionAlarm();
    }

    public boolean getChecked() {
        return checked;
    }

    public void setchecked(boolean checked) {
        this.checked = checked;
    }

    private void actionAlarm() {
        if (checked == false) {
            String hhmm = time.split(" ")[0];
            String ddmmyyyy = time.split(" ")[1];
            String []hhmmSplit = hhmm.split(":");
            String []ddmmyyyySplit = ddmmyyyy.split("/");
            Date date = new Date( Integer.parseInt(ddmmyyyySplit[2]) - 1900,
                    Integer.parseInt(ddmmyyyySplit[1]) - 1,
                    Integer.parseInt(ddmmyyyySplit[0]),
                    Integer.parseInt(hhmmSplit[0]),
                    Integer.parseInt(hhmmSplit[1]), 0);

            serviceIntent = new Intent(context, AlarmService.class);
            if (milliseconds == 0) {
                milliseconds = date.getTime();
            }
            serviceIntent.putExtra("oldID", milliseconds);
            milliseconds = date.getTime();
//            Log.e(TAG, "Create ItemClassContent: " + milliseconds);
            serviceIntent.putExtra("timeInMillis", milliseconds);
            serviceIntent.putExtra("path", path);
            serviceIntent.putExtra("time", time);
            context.startForegroundService(serviceIntent);
        }
    }
}
