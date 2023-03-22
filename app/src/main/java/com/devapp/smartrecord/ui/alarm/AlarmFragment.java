package com.devapp.smartrecord.ui.alarm;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.databinding.FragmentAlarmBinding;
import com.devapp.smartrecord.ui.folder.FolderViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlarmFragment extends Fragment implements ItemClassContentAdapter.OnItemClickListener {
    private FragmentAlarmBinding binding;
    private RecyclerView rcvFuture;
    private ItemClassContentAdapter adapterItemFuture;
    private ArrayList<ItemClassContent> listItemFuture;
    private RecyclerView rcvPast;
    private ItemClassContentAdapter adapterItemPast;
    private ArrayList<ItemClassContent> listItemPast;

    private int lastSelectedYear;
    private int lastSelectedMonth;
    private int lastSelectedDayOfMonth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlarmViewModel alarmViewModel =
                new ViewModelProvider(this).get(AlarmViewModel.class);

        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textAlarm;
//        alarmViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

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
        // Handle the click event here
//        Toast.makeText(this, "Clicked on position " + position + ", item text: ", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onItemClick: " + position);

        final boolean isSpinnerMode = true;

        // Date Select Listener.
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {

//                editTextDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
//
//                lastSelectedYear = year;
//                lastSelectedMonth = monthOfYear;
//                lastSelectedDayOfMonth = dayOfMonth;
            }
        };

        DatePickerDialog datePickerDialog = null;

        if(isSpinnerMode)  {
            // Create DatePickerDialog:
            datePickerDialog = new DatePickerDialog(getContext(),
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);
        }
        // Calendar Mode (Default):
        else {
            datePickerDialog = new DatePickerDialog(getContext(),
                    dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);
        }

        // Show
        datePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<ItemClassContent> getListViewFuture() {
        listItemFuture.add(new ItemClassContent("HCMUS - 23/03/3023", "12:15 29/03/2023", false));
        listItemFuture.add(new ItemClassContent("HCMUS - 23/03/3023", "12:15 29/03/2023", false));

        return listItemFuture;
    }
    private List<ItemClassContent> getListViewPast() {
        listItemPast.add(new ItemClassContent("HCMUS - 23/03/3023", "12:15 29/03/2023", true));
        listItemPast.add(new ItemClassContent("HCMUS - 23/03/3023", "12:15 29/03/2023", true));

        return listItemPast;
    }
}
