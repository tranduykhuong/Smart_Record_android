package com.devapp.smartrecord.ui.folder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ReplayActivity;
import com.devapp.smartrecord.ui.home.Audio;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileInFolder extends AppCompatActivity implements FileInFolderAdapter.OnItemClickListener{
    private List<Audio> audioList;
    private RecyclerView rcvFileInFolderAudio;
    private FileInFolderAdapter fileInFolderAudioAdapter;
    private File recordingsDirectory;
    private double sumCapacity = 0;
    private int sumAmountFile = 0;
    private File[] files;
    private int size;
    private String nameFolder;
    private java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.##");
    private TextView totalAmountAudio, totalCapacityAudio;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_insert);
        getSupportActionBar().hide();

        Bundle bundle = getIntent().getExtras();
        nameFolder = bundle.getString("nameFolder");

        rcvFileInFolderAudio = (RecyclerView) this.findViewById(R.id.insert_fragment_rcv_list);
        totalAmountAudio = findViewById(R.id.insert_fragment_amount);
        totalCapacityAudio = findViewById(R.id.insert_fragment_size);
        TextView btnDestroy = (TextView) this.findViewById(R.id.insert_fragment_destroy);
        TextView amountFile = (TextView) this.findViewById(R.id.insert_fragment_amount);
        TextView sizeTotalFile = (TextView) this.findViewById(R.id.insert_fragment_size);
        TextView titleFolder = (TextView) this.findViewById(R.id.insert_fragment_header);
        final androidx.appcompat.widget.SearchView searchView = (SearchView) this.findViewById(R.id.insert_fragment_search_edt);

        titleFolder.setText(nameFolder);

        btnDestroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Lấy InputMethodManager từ Context
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(this).getSystemService(Context.INPUT_METHOD_SERVICE);

        // Lắng nghe sự kiện chạm màn hình
        this.findViewById(android.R.id.content).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Kiểm tra xem SearchView có đang được chỉnh sửa không
                if (!searchView.isIconified()) {
                    // Ẩn bàn phím và hủy focus của SearchView
                    searchView.clearFocus();
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                }
            }
            return false;
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                String searchText = newText.toLowerCase().trim();
                List<Audio> filteredList = new ArrayList<>();

                for (Audio item : audioList) {
                    String itemText = item.getName().toLowerCase();
                    if (itemText.contains(searchText)) {
                        filteredList.add(item);
                    }
                }
                if (filteredList.isEmpty()) {
                    // Hiển thị message thông báo không tìm thấy kết quả tương ứng
                    Toast.makeText(getApplicationContext(), "Không tìm thấy kết quả phù hợp", Toast.LENGTH_SHORT).show();
                }

                // Đặt danh sách đã lọc vào adapter và cập nhật adapter
                fileInFolderAudioAdapter.filterList(filteredList);
                fileInFolderAudioAdapter.notifyDataSetChanged();

                return true;
            }
        });

        rcvFileInFolderAudio.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        getAudioList();
        String strFileSize = decimalFormat.format(1.0 * sumCapacity / 1024);
        amountFile.setText(sumAmountFile + "");
        sizeTotalFile.setText(strFileSize + "");
        fileInFolderAudioAdapter = new FileInFolderAdapter(getApplicationContext(), this);

        fileInFolderAudioAdapter.setData(audioList);
        fileInFolderAudioAdapter.setNameFolder(nameFolder);
        rcvFileInFolderAudio.setAdapter(fileInFolderAudioAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClickFile(String name, int position){
        Intent intent = new Intent(this, ReplayActivity.class);
        intent.putExtra("Name", name);
        intent.setAction("FromHome");
        startActivity(intent);
    }
    @Override
    public void onItemClick(int position) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (recordingsDirectory != null && recordingsDirectory.listFiles() != null)
            totalAmountAudio.setText(String.valueOf(recordingsDirectory.listFiles().length));
        if(sumCapacity >= 1024) {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
        }
        else {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity));
        }
    }
    @Override
    public void onItemClickConvert(int position) {
        audioList.addAll(getAudioList()); // cập nhật danh sách dữ liệu mới
        fileInFolderAudioAdapter.setData(audioList); // đặt lại danh sách dữ liệu cho adapter
        fileInFolderAudioAdapter.notifyDataSetChanged(); // thông báo cho adapter biết rằng dữ liệu đã thay đổi và cần phải cập nhật lại
    }

    private List<Audio> getAudioList() {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.##");
        audioList = new ArrayList<>();
        recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/" + nameFolder);
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
                }
            }
        }
        audioList.sort(new Comparator<Audio>() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public int compare(Audio audio1, Audio audio2) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(audio1.getCreateDate());
                    date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(audio2.getCreateDate());
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

        return audioList;
    }

    private void getFile(File file){
        String fileName = file.getName();
        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
        sumCapacity += (1.0 * file.length() / (1024 * 1.0));
        Date lastModifiedDate = new Date(file.lastModified());
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
        audioList.add(new Audio(fileName, getFileDuration(file), String.valueOf(fileSize), formattedDate, R.drawable.ic_play_outline));
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
