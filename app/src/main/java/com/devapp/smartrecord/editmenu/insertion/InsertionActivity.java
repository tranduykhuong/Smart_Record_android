package com.devapp.smartrecord.editmenu.insertion;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;
import com.devapp.smartrecord.ui.home.HomeAudioAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InsertionActivity extends AppCompatActivity {
    private ImageButton btnBack, btnInsert;

//    private TextView twMin;
//    private TextView twMax;
    private TextView timeMax;
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
    private String name, maxTime, size, day;
    private List<Entry> entries;
    //    String audioFilePath = "/storage/emulated/0/Music/Samsung/Over_the_Horizon.mp3";
    String audioFilePath = "/storage/emulated/0/Recordings/139-40 Đ. Trần Hưng Đạo (2).mp3";

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
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_insertion);
        getSupportActionBar().hide();

        btnBack = findViewById(R.id.edt_insert_btn_back);
        btnBack.setOnClickListener(view -> onBackPressed());


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        progressWidth = displayMetrics.widthPixels;
        realWidth = displayMetrics.widthPixels;

        entries = new ArrayList<>();

        Intent intent = getIntent();
        audioFilePath = intent.getStringExtra("PATH_KEY");

//        twMin = findViewById(R.id.tw_min);
//        twMax = findViewById(R.id.tw_max);
        hrzScrollView = findViewById(R.id.insert_horizontal);
        timeMax = findViewById(R.id.insert_time_max);

        btnPlay = findViewById(R.id.insert_play_btn);

        btnInsert = findViewById(R.id.insert_btn);
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InsertionActivity.this, InsertionListFile.class);
                startActivityForResult(intent, 1);
            }
        });

        TextView titleFile = findViewById(R.id.insert_name_file);
        String[] nameFile = audioFilePath.split("/");
        titleFile.setText(nameFile[nameFile.length - 1]);

        SeekBar seekBar = findViewById(R.id.insert_seekbar);
        TextView txtCurTime = findViewById(R.id.insert_time_current);
        RelativeLayout itemInsert = findViewById(R.id.insert_item_view);
        TextView maxTimeInsertFile = findViewById(R.id.insert_item_amount);
        TextView sizeInsertFile = findViewById(R.id.insert_item_size);
        TextView dayInsertFile = findViewById(R.id.insert_item_hours);

        seekBar.setProgress(0);
        txtCurTime.setText("00:00:00");

        btnPlay.setOnClickListener(v -> {
            ImageView imageView = (ImageView) v;
            if (imageView.getTag() == null || imageView.getTag().equals("ic_play")) {
                imageView.setImageResource(R.drawable.ic_pause);
                imageView.setTag("ic_pause");

                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
            } else {
                imageView.setImageResource(R.drawable.ic_play);
                imageView.setTag("ic_play");

                mediaPlayer.pause();
            }
        });

        TextView btnDestroyInsert = findViewById(R.id.insert_destroy);
        btnDestroyInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Khởi tạo MediaPlayer và Visualizer
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        duration = mediaPlayer.getDuration();
        timeMax.setText(formatTime(duration));


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
        chart.setGridBackgroundColor(Color.WHITE);

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

//        // RANGE SEEKBAR
//        RangeSeekBar<Integer> rangeSeekBar = findViewById(R.id.rangeSeekBar);
//        rangeSeekBar.setRangeValues(0, 100);
//        twMin.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
//        twMax.setText(String.format(Locale.getDefault(), "%02d:%02d", (duration / 1000) / 60, (duration / 1000) % 60));
//
//        rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {
//            // Handle the range seek bar values changed event
//            percentMin = minValue;
//            percentMax = maxValue;
//            int minSecond = (int) (minValue / 100f * (duration / 1000));
//            int maxSecond = (int) (maxValue / 100f * (duration / 1000));
//            twMin.setText(String.format(Locale.getDefault(), "%02d:%02d", minSecond / 60, minSecond % 60));
//            twMax.setText(String.format(Locale.getDefault(), "%02d:%02d", maxSecond / 60, maxSecond % 60));
//
//            updateChart();
//
//            int currentPosition = (int) (minValue / 100f * duration);
//            mediaPlayer.seekTo(currentPosition);
//
//            int delta = (int) ((percentMax - percentMin) / 100f * duration);
//            timeMax.setText(String.format(Locale.getDefault(), "%02d:%02d", (delta / 1000) / 60, (delta / 1000) % 60));
//        });

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
//            flagPlaying = false;
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
        chart.setData(lineData);
        chart.invalidate();

        chart.highlightValue(0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Bundle bundle = data.getBundleExtra("result");
            name = bundle.getString("name", "");
            size = bundle.getString("size", "");
            maxTime = bundle.getString("maxTime", "");
            day = bundle.getString("day", "");
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
        }
    }
}