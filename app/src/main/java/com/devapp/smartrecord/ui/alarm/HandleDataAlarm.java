package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HandleDataAlarm {
    Context context;

    private static HandleDataAlarm sgt;
    private ArrayList<ItemClassContent> listItemPast;
    private ArrayList<ItemClassContent> listItemFuture;

    private DatePickerDialog datePickerDialog = null;
    private TimePickerDialog timePickerDialog = null;

    private Set<String> dataSet;

    private int hourSelected;
    private int minuteSelected;
    private int daySelected;
    private int monthSelected;
    private int yearSelected;

    public static HandleDataAlarm getInstance(Context context) {
        if (sgt == null) {
            sgt = new HandleDataAlarm(context);
        }
        return sgt;
    }

    private HandleDataAlarm(Context context) {
        this.context = context;
        loadRemindData();
    }

    public void setChecked(String time) {
        for (int i = 0; i < listItemFuture.size(); i++) {
            if (listItemFuture.get(i).getTime().equals(time)) {
                listItemFuture.get(i).setchecked(true);
            }
        }
        saveRemindData();
        loadRemindData();
    }

    public void addReminder(String path) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                hourSelected = hourOfDay;
                minuteSelected = minute;

                Calendar calendar1 = Calendar.getInstance();
                if (yearSelected == calendar1.get(Calendar.YEAR)
                        && monthSelected == calendar1.get(Calendar.MONTH) + 1
                        && daySelected == calendar1.get(Calendar.DAY_OF_MONTH)) {
                    if (hourSelected < calendar1.get(Calendar.HOUR_OF_DAY)
                            || (hourSelected == calendar1.get(Calendar.HOUR_OF_DAY)
                            && minuteSelected <= calendar1.get(Calendar.MINUTE))) {
                        Toast.makeText(context,
                                "Thời gian thiết đặt không hợp lệ! Thời gian nên được chọn ở tương lai!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                String time = hourSelected + ":" + minuteSelected + " "
                        + daySelected + "/" + monthSelected + "/" + yearSelected;

                listItemFuture.add(new ItemClassContent(context, path, time, false));

                int[] duration = getDurationTime(time);
                Toast.makeText(context, "Nhắc nhở được đặt sau " + duration[0]
                        + " ngày, " + duration[1] + " giờ và " + duration[2] + " phút kể từ bây giờ", Toast.LENGTH_SHORT).show();

                saveRemindData();
            }
        }, hour, minute, true);

        // DATE
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(context,
                R.style.CustomDatePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                daySelected = dayOfMonth;
                monthSelected = monthOfYear + 1;
                yearSelected = year;
            }
        }, year, month, day);

        // Add a listener to the OK button of DatePickerDialog
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                daySelected = datePickerDialog.getDatePicker().getDayOfMonth();
                monthSelected = datePickerDialog.getDatePicker().getMonth() + 1;
                yearSelected = datePickerDialog.getDatePicker().getYear();

                timePickerDialog.show();
                dialog.dismiss();
            }
        });
        calendar = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMinDate(
                calendar.getTimeInMillis() - 1000);

        // Show
        if (isValidContextForDialog(context)) {
            Log.e(TAG, "addReminder: true");
            datePickerDialog.show();
        } else {
            Log.e(TAG, "addReminder: false");
            Toast.makeText(context, "Có lỗi xảy ra! Vui lòng khởi động lại ứng dụng!", Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean isValidContextForDialog(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                return true;
            }
        }
        return false;
    }

    public void onChangeTimeAlarmFurture(int position, ItemClassContentAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        if (listItemFuture.get(position).getTime() != "") {
            String time = listItemFuture.get(position).getTime();
            String hhmm = time.split(" ")[0];
            String ddmmyyyy = time.split(" ")[1];
            String []hhmmSplit = hhmm.split(":");
            String []ddmmyyyySplit = ddmmyyyy.split("/");
            Date date = new Date( Integer.parseInt(ddmmyyyySplit[2]) - 1900,
                    Integer.parseInt(ddmmyyyySplit[1]) - 1,
                    Integer.parseInt(ddmmyyyySplit[0]),
                    Integer.parseInt(hhmmSplit[0]),
                    Integer.parseInt(hhmmSplit[1]), 0);
            if (date.getTime() > System.currentTimeMillis()) {
                calendar.set(Integer.parseInt(ddmmyyyySplit[2]),
                        Integer.parseInt(ddmmyyyySplit[1]) - 1,
                        Integer.parseInt(ddmmyyyySplit[0]),
                        Integer.parseInt(hhmmSplit[0]),
                        Integer.parseInt(hhmmSplit[1]));
            }
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                hourSelected = hourOfDay;
                minuteSelected = minute;

                Calendar calendar1 = Calendar.getInstance();
                if (yearSelected == calendar1.get(Calendar.YEAR)
                        && monthSelected == calendar1.get(Calendar.MONTH) + 1
                        && daySelected == calendar1.get(Calendar.DAY_OF_MONTH)) {
                    if (hourSelected < calendar1.get(Calendar.HOUR_OF_DAY)
                            || (hourSelected == calendar1.get(Calendar.HOUR_OF_DAY)
                            && minuteSelected <= calendar1.get(Calendar.MINUTE))) {
                        Toast.makeText(context,
                                "Thời gian thiết đặt không hợp lệ! Thời gian nên được chọn ở tương lai!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                listItemFuture.get(position).setTime(hourSelected + ":" + minuteSelected + " "
                        + daySelected + "/" + monthSelected + "/" + yearSelected);

                adapter.notifyItemChanged(position);

                int[] duration = getDurationTime(listItemFuture.get(position).getTime());
                Toast.makeText(context, "Nhắc nhở được đặt sau " + duration[0]
                        + " ngày, " + duration[1] + " giờ và " + duration[2] + " phút kể từ bây giờ", Toast.LENGTH_SHORT).show();
            }
        }, hour, minute, true);

        // DATE
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(context,
                R.style.CustomDatePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                daySelected = dayOfMonth;
                monthSelected = monthOfYear + 1;
                yearSelected = year;
            }
        }, year, month, day);

        // Add a listener to the OK button of DatePickerDialog
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                daySelected = datePickerDialog.getDatePicker().getDayOfMonth();
                monthSelected = datePickerDialog.getDatePicker().getMonth() + 1;
                yearSelected = datePickerDialog.getDatePicker().getYear();

                timePickerDialog.show();
                dialog.dismiss();
            }
        });
        calendar = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMinDate(
                calendar.getTimeInMillis() - 1000);

        // Show
        Log.e(TAG, "onChangeTimeAlarmFurture: " + context);
        datePickerDialog.show();
    }

    public List<ItemClassContent> getListViewFuture() {
        return listItemFuture;
    }
    public List<ItemClassContent> getListViewPast() {
//        ArrayList<String> audioList = new ArrayList<>();
//        ContentResolver contentResolver = context.getContentResolver();
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        String[] projection = {MediaStore.Audio.Media.TITLE};
//        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
//                audioList.add(title);
//            }
//            cursor.close();
//        }
//        for(int i = 0; i < audioList.size()-10; i++) {
//            String item = audioList.get(i);
//            listItemFuture.add(new ItemClassContent(context, item, "12:15 31/03/2023", false));
//            listItemPast.add(new ItemClassContent(context, item, "12:15 28/03/2023", true));
//        }
//        saveRemindData();

        return listItemPast;
    }

    public void saveRemindData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("remindData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> set = new HashSet<>();
        for (int i=0; i<listItemPast.size(); i++) {
            ItemClassContent item = listItemPast.get(i);
            set.add(item.getPath() + "`" + item.getTime() + "`" + item.getChecked());
        }
        for (int i=0; i<listItemFuture.size(); i++) {
            ItemClassContent item = listItemFuture.get(i);
            set.add(item.getPath() + "`" + item.getTime() + "`" + item.getChecked());
        }
        editor.putStringSet("remindDataKey", set);
        editor.apply();
    }

    public void loadRemindData() {
        listItemFuture = new ArrayList<>();
        listItemPast = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("remindData", Context.MODE_PRIVATE);
        if (sharedPreferences == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();

        dataSet = sharedPreferences.getStringSet("remindDataKey", null);

        if (dataSet != null) {
            ArrayList<String> data = new ArrayList<>(dataSet);
            for (int i=0; i<data.size(); i++) {
                String item[] = data.get(i).split("`");
                String hhmm = item[1].split(" ")[0];
                String ddmmyyyy = item[1].split(" ")[1];
                String []hhmmSplit = hhmm.split(":");
                String []ddmmyyyySplit = ddmmyyyy.split("/");
                Date date = new Date( Integer.parseInt(ddmmyyyySplit[2]) - 1900,
                        Integer.parseInt(ddmmyyyySplit[1]) - 1,
                        Integer.parseInt(ddmmyyyySplit[0]),
                        Integer.parseInt(hhmmSplit[0]),
                        Integer.parseInt(hhmmSplit[1]), 0);
                if (date.getTime() > System.currentTimeMillis() && item[2].equals("false")) {
                    listItemFuture.add(new ItemClassContent(context, item[0], item[1], false));
                } else {
                    listItemPast.add(new ItemClassContent(context, item[0], item[1], true));
                }
            }
            saveRemindData();
        }
    }

    public int[] getDurationTime(String time) {
        String hhmm = time.split(" ")[0];
        String ddmmyyyy = time.split(" ")[1];
        String []hhmmSplit = hhmm.split(":");
        String []ddmmyyyySplit = ddmmyyyy.split("/");
        Date date = new Date( Integer.parseInt(ddmmyyyySplit[2]) - 1900,
                Integer.parseInt(ddmmyyyySplit[1]) - 1,
                Integer.parseInt(ddmmyyyySplit[0]),
                Integer.parseInt(hhmmSplit[0]),
                Integer.parseInt(hhmmSplit[1]), 0);

        long duration = date.getTime() - System.currentTimeMillis() + 1000;
        int seconds = (int) (duration / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;

        return new int[] {days, hours%24, minutes%60};
    }
}
