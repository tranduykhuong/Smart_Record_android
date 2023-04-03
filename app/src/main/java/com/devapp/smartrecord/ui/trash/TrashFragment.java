package com.devapp.smartrecord.ui.trash;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentTrashBinding;
import com.devapp.smartrecord.ui.home.Audio;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrashFragment extends Fragment implements TrashAdapter.OnItemClickListenerTrash{
    private RecyclerView recyclerView;
    private TrashAdapter trashAdapter;
    private List<Item> itemList;
    private File trashDirectory;
    private File[] files;
    private SearchView searchView;
    private TextView totalCapacityItem;
    private TextView totalAmountAudio;
    private TextView capacityUnit;
    private double sumCapacity = 0;
    private int size;
    private FragmentTrashBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TrashViewModel trashViewModel = new ViewModelProvider(this).get(TrashViewModel.class);
        binding = FragmentTrashBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        totalCapacityItem = binding.trashCapacityAudio;
        totalAmountAudio = binding.trashAmountAudio;
        capacityUnit = binding.capacityAudioUnit;

        searchView = binding.searchViewAudioTrash;
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        recyclerView = binding.trashRcvAudioList;
        recyclerView.setLayoutManager(new LinearLayoutManager((getContext())));
        trashAdapter = new TrashAdapter(getContext(), this);

        trashAdapter.setData(getItemList());
        recyclerView.setAdapter(trashAdapter);

        if (trashDirectory != null && trashDirectory.listFiles() != null)
            totalAmountAudio.setText(String.valueOf(trashDirectory.listFiles().length));
        if(sumCapacity >= 1024) {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity));
        }

        return root;
    }

    @NonNull
    private List<Item> getItemList() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        itemList = new ArrayList<>();
        trashDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/TrashAudio/");
        if (trashDirectory != null && trashDirectory.listFiles() != null)
            size = trashDirectory.listFiles().length;
        if (trashDirectory.exists()) {
            files = trashDirectory.listFiles();
            if (files != null) {
                double tempCapacity = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3") || file.getName().endsWith(".aac")) {

                        String fileName = file.getName();
                        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
                        tempCapacity += (1.0 * file.length() / (1024 * 1.0));
                        Date lastModifiedDate = new Date(file.lastModified());
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        itemList.add(new Item(fileName, getFileDuration(file), String.valueOf(fileSize), formattedDate, R.drawable.ic_play_audio_item));
                    }
                }
                if(sumCapacity == 0) {
                    sumCapacity += tempCapacity;
                } else {
                    sumCapacity = tempCapacity;
                }

            }
        }
        return itemList;
    }

    @NonNull
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

    private void filterList(String text) {
        List<Item> filterList = new ArrayList<>();
        for (Item _item : itemList) {
            if (_item.getName().toLowerCase().contains(text.toLowerCase())){
                filterList.add(_item);
            }
        }
        if (filterList.isEmpty()) {
            trashAdapter.setData(null);
            Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        }else {
            trashAdapter.setData(filterList);
        }
    }

    @Override
    public void onItemClick(int position) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (trashDirectory != null && trashDirectory.listFiles() != null)
            totalAmountAudio.setText(String.valueOf(trashDirectory.listFiles().length));
        if(sumCapacity >= 1024) {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity));
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
