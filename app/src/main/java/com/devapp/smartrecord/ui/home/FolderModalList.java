package com.devapp.smartrecord.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.folder.FolderCLassContent;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FolderModalList extends AppCompatActivity implements FolderModalAdapter.OnItemClickListener{
    private List<FolderCLassContent> folderList;
    private RecyclerView rcvInsertAudio;
    private FolderModalAdapter folderMoveAdapter;
    private File recordingsDirectory;
    private double sumCapacity = 0;
    private int sumAmountFile = 0;
    private File[] files;
    private int size;
    private java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.##");


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_insert);
        getSupportActionBar().hide();

        rcvInsertAudio = (RecyclerView) this.findViewById(R.id.insert_fragment_rcv_list);
        TextView btnDestroy = (TextView) this.findViewById(R.id.insert_fragment_destroy);
        TextView amountFile = (TextView) this.findViewById(R.id.insert_fragment_amount);
        TextView sizeTotalFile = (TextView) this.findViewById(R.id.insert_fragment_size);

        btnDestroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        rcvInsertAudio.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        getAudioList();
        String strFileSize = decimalFormat.format(1.0 * sumCapacity / 1024);
        amountFile.setText(sumAmountFile + "");
        sizeTotalFile.setText(strFileSize + "");
        folderMoveAdapter = new FolderModalAdapter(getApplicationContext(), this);

        folderMoveAdapter.setData(folderList);
        rcvInsertAudio.setAdapter(folderMoveAdapter);
    }

    @Override
    public void onItemClickModal(int position){
        Toast.makeText(this, position +"", Toast.LENGTH_SHORT).show();
        Intent returnIntent = getIntent();
        returnIntent.putExtra("result", "dữ liệu cần trả về");
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private List<FolderCLassContent> getAudioList() {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.##");
        folderList = new ArrayList<>();
        recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/");
        if (recordingsDirectory != null && recordingsDirectory.listFiles() != null)
            size = recordingsDirectory.listFiles().length;
        if (recordingsDirectory.exists()) {
            files = recordingsDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") || file.getName().endsWith(".m4a")) {
                        getFile(file);
                        sumAmountFile += 1;
                    }
                    else if (file.isDirectory()){
                        File[] listNewFile = file.listFiles();
                        for (File newFile : listNewFile){
                            if (newFile.isFile() && newFile.getName().endsWith(".mp3") || newFile.getName().endsWith(".wav") || newFile.getName().endsWith(".m4a")) {
                                getFile(newFile);
                                sumAmountFile += 1;
                            }
                        }
                    }
                }
            }
            Collections.sort(folderList, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent forlder2) {
                    Date date1 = null;
                    Date date2 = null;
                    try {
                        date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(folder1.getHour());
                        date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(forlder2.getHour());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    } else {
                        return 0;
                    }
                }
            });
        }

        return folderList;
    }

    private void getFile(File file){
        String fileName = file.getName();
        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
        sumCapacity += (1.0 * file.length() / (1024 * 1.0));
        Date lastModifiedDate = new Date(file.lastModified());
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
        folderList.add(new FolderCLassContent(fileName, getFileDuration(file), String.valueOf(fileSize), formattedDate, R.drawable.ic_pink500_folder));
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
}
