package com.devapp.smartrecord.editmenu.insertion;

import static android.content.ContentValues.TAG;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.ConfigurationClass;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class InsertionActivity extends AppCompatActivity {
    private ImageButton btnBack, btnInsert;
    private TextView timeMax, btnExport;
    private ImageView btnPlay;
    private MediaPlayer mediaPlayer;
    private LineChart chart;
    private Runnable highlight;
    private final Handler handler = new Handler();
    private int progressWidth;
    private int realWidth;
    private float rate1, rate2, rate3;
    private int duration;
    private int percentMin = 0;
    private int percentMax = 100;
    private byte[] waveform;
    ArrayList<Float> wf;
    private HorizontalScrollView hrzScrollView;
    private TextView nameInsertFile,maxTimeInsertFile, sizeInsertFile, dayInsertFile;
    private RelativeLayout itemInsert;
    private Audio file;
    private final String tempPath = Environment.getExternalStorageDirectory().toString() + "/Recordings/";
    private File outputFileInsert;
    private boolean inserted = false, exported = false, isPlay = false;
    private List<Entry> entries;
    private String audioFilePath, fileNameRoot, finalNameFile;
    private String[] nameFile;
    private int currPosition = 0;
    private ConfigurationClass config;


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Dừng phát âm thanh và giải phóng tài nguyên
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(highlight);

        if(inserted)
        {
            if (!exported){
                File fileDl = new File(tempPath + finalNameFile);
                File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
                if (fileDel.exists()) {
                    fileDel.delete();
                }
            }
        }
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_insertion);
        getSupportActionBar().hide();

        config = new ConfigurationClass(this);
        config.getConfig();

        btnBack = findViewById(R.id.edt_insert_btn_back);
        btnBack.setOnClickListener(view -> {
            if(inserted)
            {
                File fileDl = new File(tempPath + finalNameFile);
                File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
                if (fileDel.exists()) {
                    fileDel.delete();
                }
            }
            onBackPressed();
        });


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        progressWidth = displayMetrics.widthPixels;
        realWidth = displayMetrics.widthPixels;

        entries = new ArrayList<>();

        Intent intent = getIntent();
        audioFilePath = intent.getStringExtra("PATH_KEY");

        hrzScrollView = findViewById(R.id.insert_horizontal);
        timeMax = findViewById(R.id.insert_time_max);
        btnExport = findViewById(R.id.insert_export);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inserted){
                    // Dừng phát âm thanh và giải phóng tài nguyên
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    handler.removeCallbacks(highlight);

                    Toast.makeText(InsertionActivity.this, getApplicationContext().getText(R.string.announce_save_successfully), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    exported = true;
                }
                else{
                    exported = false;
                    Toast.makeText(InsertionActivity.this, getApplicationContext().getText(R.string.insert_not_file), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPlay = findViewById(R.id.insert_play_btn);

        btnInsert = findViewById(R.id.insert_btn);
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inserted){
                    if (isPlay){
                        Toast.makeText(InsertionActivity.this, getApplicationContext().getText(R.string.insert_warning_insertfile), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent = new Intent(InsertionActivity.this, InsertionListFile.class);
                        startActivityForResult(intent, 1);
                    }
                }
                else{
                    btnInsert.setEnabled(true);
                    Toast.makeText(InsertionActivity.this, R.string.inserted, Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView titleFile = findViewById(R.id.insert_name_file);
        nameFile = audioFilePath.split("/");
        fileNameRoot = nameFile[nameFile.length - 1];
        titleFile.setText(fileNameRoot);

        SeekBar seekBar = findViewById(R.id.insert_seekbar);
        TextView txtCurTime = findViewById(R.id.insert_time_current);
        itemInsert = findViewById(R.id.insert_item_view);
        maxTimeInsertFile = findViewById(R.id.insert_item_amount);
        sizeInsertFile = findViewById(R.id.insert_item_size);
        dayInsertFile = findViewById(R.id.insert_item_hours);
        nameInsertFile = findViewById(R.id.insert_item_title);

        if (!inserted){
            itemInsert.setVisibility(View.GONE);
        }

        seekBar.setProgress(0);
        txtCurTime.setText("00:00:00");

        btnPlay.setOnClickListener(v -> {
            ImageView imageView = (ImageView) v;
            if (imageView.getTag() == null || imageView.getTag().equals("ic_play")) {
                imageView.setImageResource(R.drawable.ic_pause);
                imageView.setTag("ic_pause");

                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
                isPlay = true;
            } else {
                imageView.setImageResource(R.drawable.ic_play);
                imageView.setTag("ic_play");
                currPosition = mediaPlayer.getCurrentPosition();

                mediaPlayer.pause();
                isPlay = false;
            }
        });

        TextView btnDestroyInsert = findViewById(R.id.insert_destroy);
        btnDestroyInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inserted)
                {
                    File fileDl = new File(tempPath + finalNameFile);
                    File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
                    if (fileDel.exists()) {
                        fileDel.delete();
                    }
                }
                onBackPressed();
            }
        });

        initMedia(audioFilePath);

        // Khởi tạo biểu đồ tần số âm thanh
        chart = findViewById(R.id.insert_chart);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(true);
        chart.getDescription().setEnabled(false);
        chart.setVisibleXRangeMinimum(60 * 1000f);
        // Vẽ lưới nền và thiết lập màu nền cho lưới
        chart.setDrawGridBackground(true);
//        chart.setGridBackgroundColor(Color.WHITE);
        if (config.getThemeMode() == 1) {
            chart.setGridBackgroundColor(Color.parseColor("#24272C"));
        } else {
            chart.setGridBackgroundColor(Color.WHITE);
        }

        // Thiết lập khoảng cách giữa các đường lưới trên trục X và Y
        chart.getXAxis().setGranularity(1000f);
        chart.getXAxis().setSpaceMin(1000f);
        chart.getXAxis().setDrawLabels(false); //
        chart.getXAxis().setEnabled(false); //
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);


        // Đọc dữ liệu từ tệp
        File file = new File(audioFilePath);
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            inputStream.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = buffer.asIntBuffer();

        wf = new ArrayList<>();
        while (intBuffer.hasRemaining()) {
            int sample = intBuffer.get();
            float amplitude = (float) (Math.abs((float) sample) / 32768.0);
            // sử dụng cường độ âm thanh tại mỗi mẫu dữ liệu ở đây
            wf.add(amplitude);
        }
        Log.e(TAG, "wf: " + wf.size());

        // Chuyển đổi các mẫu âm thanh sang dạng số thực và chuẩn hóa
        double[] samples = new double[data.length / 2];
        for (int i = 0; i < samples.length; i++) {
            short sample = (short) (((data[i * 2 + 1] & 0xff) << 8) | (data[i * 2] & 0xff));
            samples[i] = (double) sample / Short.MAX_VALUE;
        }

        // Chuyển sang waveform
        waveform = new byte[samples.length + 1];
        for (int i = 0; i < samples.length; i = i + 1) {
            waveform[i] = (byte) (samples[i] * 100);
        }


        Log.e(TAG, "Waveform: " + waveform.length);
        Log.e(TAG, "length: " + duration);

        // Cập nhật vẽ chart
        updateChart();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                txtCurTime.setText(formatTime(progress));
                seekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
            btnPlay.setImageResource(R.drawable.ic_play);
            btnPlay.setTag("ic_play");
            seekBar.setProgress(0);
            txtCurTime.setText("00:00:00");
            isPlay = false;
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                highlight = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                            txtCurTime.setText(formatTime(currentPosition));
                            // do something with currentPosition
                            chart.highlightValue((int) (currentPosition * (waveform.length * 1f / (duration - 2.2))), 0);

                            rate1 = 1.0f * realWidth / chart.getWidth();
                            rate2 = 1.0f * currentPosition / duration;

                            if (rate3 > 0.01 && rate2 > rate3) {
                                rate3 = rate3 + rate1;
                                hrzScrollView.smoothScrollTo(progressWidth, 0);
                                progressWidth = progressWidth + realWidth;
                            }
                            handler.postDelayed(this, 10); // update every 1 second

                            if (currentPosition > percentMax / 100f * duration) {
                                mediaPlayer.pause();
                                mediaPlayer.seekTo((int) (percentMin / 100f * duration));

                                btnPlay.setImageResource(R.drawable.ic_play);
                                btnPlay.setTag("ic_play");
                            }
                        }
                    }
                };
                handler.post(highlight);
            }
        });
    }

    private void initMedia(String path){
        // Khởi tạo MediaPlayer và Visualizer
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        duration = mediaPlayer.getDuration();
        timeMax.setText(formatTime(duration));
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int duration) {
        int hours = duration / 3600000;
        int minutes = (duration % 3600000) / 60000;
        int seconds = ((duration % 3600000) % 60000) / 1000;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateChart() {
        int curMinWave = (int) (waveform.length * (percentMin / 100f));
        int curMaxWave = (int) (waveform.length * (percentMax / 100f));
        Log.e(TAG, "updateChart: " + curMinWave + " " + curMaxWave);

        entries.clear();
        for (int i = curMinWave; i < curMaxWave; i = i + 134) {
            if (Math.abs(waveform[i]) < 60)
                entries.add(new Entry(i, waveform[i] / 10));
            else if (Math.abs(waveform[i]) < 80)
                entries.add(new Entry(i, waveform[i] / 6));
            else if (Math.abs(waveform[i]) < 90)
                entries.add(new Entry(i, waveform[i] / 4));
            else if (Math.abs(waveform[i]) < 116)
                entries.add(new Entry(i, waveform[i] / 3));
            else
                entries.add(new Entry(i, waveform[i] / 2));
        }
        Log.e(TAG, "entries: " + entries.size());

        int range = Math.min(entries.size() * 2, 9000);
        chart.setMinimumWidth(range);

        rate3 = 1.0f * progressWidth / range;

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.BLACK);
        dataSet.setLineWidth(1f);
        dataSet.setHighlightLineWidth(2f);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        LineData lineData = new LineData(dataSet);
        if (config.getThemeMode() == 1) {
            dataSet.setColor(Color.WHITE);
        } else {
            dataSet.setColor(Color.BLACK);
        }
        chart.setData(lineData);
        chart.invalidate();

        chart.highlightValue(0, 0);
    }

    private void insertAudio(String baseAudioFilePath, String insertAudioFilePath, int startTime) {
        if (startTime != 0){
            File tmp1 = new File(tempPath + "file1_1.mp3");
            File tmp2 = new File(tempPath + "file1_2.mp3");

            String currentTime = formatTime(startTime);

            String divideTime = currentTime;

            File ipfile = new File(baseAudioFilePath);
            // Lệnh để chia file
            String[] commandPart1 = {
                    "-i", String.valueOf(ipfile),
                    "-c:a", "mp3", // Mã codec âm thanh là mp3
                    "-t", divideTime, // Thời gian của phần 1 (đơn vị: giây)
                    tmp1.getAbsolutePath()
            };

            String[] commandPart2 = {
                    "-i", String.valueOf(ipfile),
                    "-c:a", "mp3", // Mã codec âm thanh là mp3
                    "-ss", divideTime, // Thời gian bắt đầu của phần 2 (đơn vị: giây)
                    tmp2.getAbsolutePath()
            };

            int rc3 = FFmpeg.execute(commandPart1);
            int rc4 = FFmpeg.execute(commandPart2);

            String[] command = {
                    "-i", tmp1.getAbsolutePath(),
                    "-i", insertAudioFilePath,
                    "-i", tmp2.getAbsolutePath(),
                    "-filter_complex", "[0:a][1:a][2:a]concat=n=3:v=0:a=1[out]",
                    "-map", "[out]",
                    "-y", outputFileInsert.getAbsolutePath()
            };

            int rcConcat = FFmpeg.execute(command);

            if (rcConcat == RETURN_CODE_SUCCESS) {
                File fileDl = new File(tempPath + "file1_1.mp3");
                File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
                if (fileDel.exists()) {
                    fileDel.delete();
                }
                File fileDl1 = new File(tempPath + "file1_2.mp3");
                File fileDel1 = new File(String.valueOf(fileDl1.getAbsoluteFile()));
                if (fileDel1.exists()) {
                    fileDel1.delete();
                }
            } else {
                Log.i(TAG, "Error: " + Config.getLastCommandOutput());
            }

            initMedia(outputFileInsert.getAbsolutePath());
        }
        else{
            String[] command = {
                    "-i", insertAudioFilePath,
                    "-i", baseAudioFilePath,
                    "-filter_complex", "[0:a][1:a]concat=n=2:v=0:a=1[out]",
                    "-map", "[out]",
                    "-y", outputFileInsert.getAbsolutePath()
            };

            int rcConcat = FFmpeg.execute(command);

            if (rcConcat == RETURN_CODE_SUCCESS) {
                File fileDl1 = new File(tempPath + "file1_2.mp3");
                File fileDel1 = new File(String.valueOf(fileDl1.getAbsoluteFile()));
                if (fileDel1.exists()) {
                    fileDel1.delete();
                }
            } else {
                Log.i(TAG, "Error: " + Config.getLastCommandOutput());
            }

            initMedia(outputFileInsert.getAbsolutePath());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Bundle bundle = data.getBundleExtra("result");
            file = (Audio) bundle.getSerializable("file");
            String pathFile = bundle.getString("path");
            inserted = bundle.getBoolean("Inserted");

            Log.e(TAG, "onActivityResult: "  + pathFile);

            if (inserted){
                itemInsert.setVisibility(View.VISIBLE);
                nameInsertFile.setText(file.getName());
                sizeInsertFile.setText(file.getSize() + " KB");
                maxTimeInsertFile.setText(file.getTimeOfAudio() + "     ");
                dayInsertFile.setText(file.getCreateDate());
//                btnExport.setTextColor(R.color.pink_500);

                String convertFileRoot = fileNameRoot.split("\\.")[0];
                finalNameFile = "Insert_" + convertFileRoot + "_" + file.getName();
                outputFileInsert = new File(tempPath, finalNameFile);
                Toast.makeText(this, getApplicationContext().getText(R.string.add_successfull), Toast.LENGTH_SHORT).show();
                insertAudio(audioFilePath, pathFile, currPosition);
            }
        }
    }
}