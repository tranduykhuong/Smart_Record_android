package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentAlarmBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmFragment extends Fragment implements ItemClassContentAdapter.OnItemClickListener {
    private FragmentAlarmBinding binding;
    private RecyclerView rcvFuture;
    private ItemClassContentAdapter adapterItemFuture;
    private ArrayList<ItemClassContent> listItemFuture;
    private RecyclerView rcvPast;
    private ItemClassContentAdapter adapterItemPast;
    private ArrayList<ItemClassContent> listItemPast;

    private DatePickerDialog datePickerDialog = null;
    private TimePickerDialog timePickerDialog = null;

    private Set<String> dataSet;

    private int hourSelected;
    private int minuteSelected;
    private int daySelected;
    private int monthSelected;
    private int yearSelected;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlarmViewModel alarmViewModel =
                new ViewModelProvider(this).get(AlarmViewModel.class);

        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        listItemFuture = new ArrayList<>();
        listItemPast = new ArrayList<>();

        loadRemindData();

        rcvFuture = binding.fileRcvListFuture;
        rcvFuture.setLayoutManager(new LinearLayoutManager(getContext()));
        listItemFuture = new ArrayList<>();
        adapterItemFuture = new ItemClassContentAdapter(getContext(), this);
        adapterItemFuture.setData(getListViewFuture());
        rcvFuture.setAdapter(adapterItemFuture);

        rcvPast = binding.fileRcvListPast;
        rcvPast.setLayoutManager(new LinearLayoutManager(getContext()));
        listItemPast = new ArrayList<>();
        adapterItemPast = new ItemClassContentAdapter(getContext(), this);
        adapterItemPast.setData(getListViewPast());
        rcvPast.setAdapter(adapterItemPast);

        return root;
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemClick(int position, boolean checked) {
        final boolean isSpinnerMode = true;

        // TIME
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                datePickerDialog.show();
                hourSelected = hourOfDay;
                minuteSelected = minute;
            }
        }, hour, minute, true);

        // DATE
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(getContext(),
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
//                Log.i(TAG, "INDEX: " + checked);
//                Log.i(TAG, "DATE: " + daySelected + "/" + monthSelected + "/" + yearSelected);
//                Log.i(TAG, "TIME: " + hourSelected + ":" + minuteSelected);
                if (checked) {
                    listItemPast.get(position).setTime(hourSelected + ":" + minuteSelected + " "
                            + daySelected + "/" + monthSelected + "/" + yearSelected);
                    adapterItemPast.notifyItemChanged(position);
                } else {
                    listItemFuture.get(position).setTime(hourSelected + ":" + minuteSelected + " "
                            + daySelected + "/" + monthSelected + "/" + yearSelected);
                    adapterItemFuture.notifyItemChanged(position);
                }

                dialog.dismiss();
            }
        });

        // Show
        timePickerDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveRemindData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<ItemClassContent> getListViewFuture() {
        if (dataSet != null) {
            ArrayList<String> data = new ArrayList<>(dataSet);
            for (int i=0; i<data.size(); i++) {
                String item[] = data.get(i).split("`");
//                Log.e(TAG, "loadRemindData: " + item[2]);
                if (item[2].equals("false")) {
                    listItemFuture.add(new ItemClassContent(getActivity(), item[0], item[1], false));
                }
            }
        }

        binding.fileAmountFuture.setText(String.valueOf(listItemFuture.size()));
        return listItemFuture;
    }
    private List<ItemClassContent> getListViewPast() {
//        ArrayList<String> audioList = new ArrayList<>();
//        ContentResolver contentResolver = getActivity().getContentResolver();
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
//            listItemPast.add(new ItemClassContent(item, "12:15 29/03/2023", false));
//            listItemPast.add(new ItemClassContent(item, "12:15 30/03/2023", true));
//        }
//        saveRemindData();
        if (dataSet != null) {
            ArrayList<String> data = new ArrayList<>(dataSet);
            for (int i=0; i<data.size(); i++) {
                String item[] = data.get(i).split("`");
//                Log.e(TAG, "loadRemindData: " + item[2]);
                if (!item[2].equals("false")) {
                    listItemPast.add(new ItemClassContent(getActivity(), item[0], item[1], true));
                }
            }
        }

        binding.fileAmountPast.setText(String.valueOf(listItemPast.size()));

        return listItemPast;
    }

    private void saveRemindData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("remindData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> set = new HashSet<>();
        for (int i=0; i<listItemPast.size(); i++) {
            ItemClassContent item = listItemPast.get(i);
            set.add(item.getTitle() + "`" + item.getTime() + "`" + item.getChecked());
        }
        for (int i=0; i<listItemFuture.size(); i++) {
            ItemClassContent item = listItemFuture.get(i);
            set.add(item.getTitle() + "`" + item.getTime() + "`" + item.getChecked());
        }
        editor.putStringSet("remindDataKey", set);
        editor.apply();
    }

    private void loadRemindData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("remindData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        dataSet = sharedPreferences.getStringSet("remindDataKey", null);
    }
}
