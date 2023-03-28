package com.devapp.smartrecord.ui.home;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentHomeBinding;
import com.devapp.smartrecord.editmenu.cut.CutActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private RecyclerView rcvHomeAudio;
    private  HomeAudioAdapter homeAudioAdapter;
    private List<Audio> audioList;

    private ImageView bluetoothBtn;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final LinearLayout linearLayoutSortType = binding.homeWrapSortType;
        linearLayoutSortType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.home_sort_type_item, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                TextView homeDateSort = binding.homeDateSort;
                TextView sortTitle = popupView.findViewById(R.id.home_sort_item_title);
                TextView sortByName = popupView.findViewById(R.id.home_sort_item_by_name);
                TextView sortByDate = popupView.findViewById(R.id.home_sort_item_by_date);
                TextView sortBySize = popupView.findViewById(R.id.home_sort_item_by_size);
                TextView cancelSort = popupView.findViewById(R.id.home_sort_item_by_destroy);

                sortTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.update();
                    }
                });

                sortByDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortByDate.getText());
                        popupWindow.dismiss();
                    }
                });

                sortByName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortByName.getText());
                        popupWindow.dismiss();
                    }
                });

                sortBySize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortBySize.getText());
                        popupWindow.dismiss();
                    }
                });

                cancelSort.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });


            }
        });

        bluetoothBtn = binding.homeBluetooth;
        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CutActivity.class);
                startActivity(intent);
            }
        });

        rcvHomeAudio = binding.homeRcvAudioList;
        rcvHomeAudio.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList = new ArrayList<>();
        homeAudioAdapter = new HomeAudioAdapter(getContext());
        homeAudioAdapter.setData(getAudioList());
        rcvHomeAudio.setAdapter(homeAudioAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @NonNull
    private List<Audio> getAudioList() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        List<Audio> audioList = new ArrayList<>();
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/Recorder/");

        if (recordingsDirectory.exists()) {
            File[] files = recordingsDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".aac") || file.getName().endsWith(".aac")) {

                        String fileName = file.getName();
                        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
                        Date lastModifiedDate = new Date(file.lastModified());
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        audioList.add(new Audio(fileName, getFileDuration(file), String.valueOf(fileSize), formattedDate, R.drawable.ic_play_audio_item));
                    }
                }
            }
        }
        return audioList;
    }

    private String getFileDuration(File file) {
        long durationInMillis = 0;

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            durationInMillis = Long.parseLong(durationStr);
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}