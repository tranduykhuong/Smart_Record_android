package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentAlarmBinding;
import com.devapp.smartrecord.databinding.FragmentFolderBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmFragment extends Fragment implements ItemClassContentAdapter.OnItemClickListener {
    private FragmentAlarmBinding binding;
    private RecyclerView rcvFuture;
    private ItemClassContentAdapter adapterItemFuture;
    private RecyclerView rcvPast;
    private ItemClassContentAdapter adapterItemPast;

    private HandleDataAlarm handleData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlarmViewModel alarmViewModel =
                new ViewModelProvider(this).get(AlarmViewModel.class);

        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        handleData = new HandleDataAlarm(getActivity());

        rcvFuture = binding.fileRcvListFuture;
        rcvFuture.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterItemFuture = new ItemClassContentAdapter(getContext(), this);
        adapterItemFuture.setData(handleData.getListViewFuture());
        rcvFuture.setAdapter(adapterItemFuture);

        rcvPast = binding.fileRcvListPast;
        rcvPast.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterItemPast = new ItemClassContentAdapter(getContext(), this);
        adapterItemPast.setData(handleData.getListViewPast());
        rcvPast.setAdapter(adapterItemPast);

        binding.fileAmountFuture.setText("" + handleData.getListViewFuture().size());
        binding.fileAmountPast.setText("" + handleData.getListViewPast().size());


        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.DATA};
        String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[] {"audio/mpeg"};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                // Lọc file mp3 theo đường dẫn
//                if (title.contains("Recordings")) {
                    Log.e(TAG, "onCreateView: " + title);
//                }
            }
            cursor.close();
        }

        return root;
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemClick(int position, boolean checked) {
        if (checked) return;

        handleData.onChangeTimeAlarmFurture(position, adapterItemFuture);
    }

    @Override
    public void onPause() {
        super.onPause();
        handleData.saveRemindData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
