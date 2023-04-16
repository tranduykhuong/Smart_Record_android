package com.devapp.smartrecord.ui.alarm;

import static android.service.controls.ControlsProviderService.TAG;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.devapp.smartrecord.services.AlarmNotifyService;

public class RemoveNotifyReceiver extends BroadcastReceiver {
    private HandleDataAlarm handleData;

    @Override
    public void onReceive(Context context, Intent intent) {
        handleData = HandleDataAlarm.getInstance(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(111);

        Intent serviceIntent = new Intent(context, AlarmNotifyService.class);
        context.stopService(serviceIntent);

        if (handleData.getListViewFuture().size() == 0) {
            notificationManager.cancel(112);
        }
    }
}
