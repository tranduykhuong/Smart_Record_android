package com.devapp.smartrecord.editmenu.divide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.EditMenuActivity;
import com.devapp.smartrecord.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DivideActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int cntBtn = 0;
    private Chronometer currTime;
    private TextView timeMax;
    private boolean isPlay = false;
    private int currentPosition = 0;
    private final Handler handler = new Handler();
    private String fileName, pathSound;
    private TextView divideNameAudio;
    private ImageButton divideBtnPlay, divideBtn;
    private Button divideBtnConfirm;
    private File outputDirectory;
    String outputDirectoryPath, inputFilePath, outputFilePath;

    // Một ArrayList để lưu trữ các giá trị thời gian
    ArrayList<String> timeList = new ArrayList<>();

    private Context context;
    private Runnable mRunnable;
    private Executor executor = Executors.newFixedThreadPool(2);
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    public DivideActivity() {
    }

    public DivideActivity(Context context) {
        this.context = context;
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_divide);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        pathSound = intent.getStringExtra("PATH_KEY");
        fileName = pathSound.substring(pathSound.lastIndexOf("/") + 1);
        boundView();
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(File file) {
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

    @SuppressLint("DefaultLocale")
    private String formatTime(int duration) {
        int hours = duration / 3600000;
        int minutes = (duration % 3600000) / 60000;
        int seconds = ((duration % 3600000) % 60000) / 1000;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void boundView(){
        divideNameAudio = this.findViewById(R.id.divide_name_audio);
        divideBtnPlay = this.findViewById(R.id.divide_btn_play);
        timeMax = this.findViewById(R.id.divide_time_of_audio);
        currTime = this.findViewById(R.id.divide_current_time_audio);
        divideBtn = this.findViewById(R.id.divide_btn_action);
        divideBtnConfirm = this.findViewById(R.id.divide_btn_confirm);

        // Đường dẫn tệp MP3 đầu vào
        inputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName;
        outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/";
        Log.e("sdfjnsdjfnskdfmksdjnfksd", inputFilePath);
        Log.e("sdfjnsdjfnskdfmksdjnfksd", outputFilePath);
        // Đường dẫn thư mục đầu ra
        outputDirectoryPath = Environment.getExternalStorageDirectory().toString() + "/Recordings/TmpAudioDivide/";
        outputDirectory = new File(Environment.getExternalStorageDirectory() + "/Recordings/TmpAudioDivide/");
        divideNameAudio.setText(fileName);

        //Mở file chuẩn bị
        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            mediaPlayer.prepare();
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            timeMax.setText(formatTime(file));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Khi bấm nút play
        divideBtnPlay.setOnClickListener(view -> {
            isPlay = !isPlay;
            if(cntBtn == 0 && isPlay)
            {
                divideBtnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.start();
                cntBtn = 1;
            }
            else if(cntBtn == 1 && !isPlay)
            {
                divideBtnPlay.setImageResource(R.drawable.ic_play_combine_main);
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
            else if(cntBtn == 1)
            {
                divideBtnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
            }

        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        currTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 200);
                    }
                };
                handler.post(mRunnable);
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            currTime.setText("00:00:00");
            isPlay = false;
            cntBtn = 0;
            divideBtnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });

        divideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    // Lấy thời gian hiện tại của bản ghi âm thanh đang phát
                    String currentTime = formatTime(currentPosition);
                    timeList.add(currentTime);

                    // Sử dụng thời gian hiện tại trong công việc cần thực hiện

                    Toast.makeText(getApplicationContext(), "Thời gian hiện tại: " + currentTime, Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "timeList: " + timeList.toString());
                } else {
                    // Hiển thị thông báo cho người dùng rằng không có âm thanh đang phát
                    Toast.makeText(getApplicationContext(), "Không có âm thanh đang phát", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Nhấn nút chia
        divideBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cutMp3ByTimeList(inputFilePath, timeList, outputFilePath);
            }
        });

    }//end bound view


    // Hàm cắt tệp mp3
    private void cutMp3(String inputFilePath, List<Long> cutPoints, String outputDirPath) {
        for (int i = 0; i < cutPoints.size(); i++) {
            long startMillis = cutPoints.get(i);
            long endMillis = cutPoints.get(i + 1);
            String outputFilePath = outputDirPath + "/output_" + i + ".mp3";
            String[] cmd = {"-ss", String.valueOf(startMillis), "-to", String.valueOf(endMillis), "-i", inputFilePath, "-c", "copy", outputFilePath};
            FFmpeg.execute(cmd);
        }
    }
    private void cutMp3ByTimeList(String inputFilePath, List<String> timeList, String outputDirPath) {
        List<Long> cutPoints = new ArrayList<>();
        for (String time : timeList) {
            long cutPoint = convertTimeToMillis(time);
            cutPoints.add(cutPoint);
        }
        Log.d("dfsdf", String.valueOf(cutPoints));

        // Kiểm tra kích thước của danh sách cutPoints
        if (cutPoints.size() >= 2) {
            // Gọi phương thức cắt MP3 với danh sách điểm cắt
            cutMp3(inputFilePath, cutPoints, outputDirPath);
        } else {
            // Xử lý khi danh sách điểm cắt không đủ phần tử
            Toast.makeText(getApplicationContext(), "Danh sách điểm cắt không đúng", Toast.LENGTH_SHORT).show();
        }
    }

    private long convertTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        long totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;
        return totalMillis;
    }

    public void changeLayoutFromDivide(View view) {
        switch (view.getId()) {
            case R.id.divide_btn_back: {
                Intent intent = new Intent(this, EditMenuActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
