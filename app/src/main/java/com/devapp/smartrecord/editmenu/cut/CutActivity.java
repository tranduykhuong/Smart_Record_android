package com.devapp.smartrecord.editmenu.cut;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devapp.smartrecord.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CutActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView nameCut;
    private TextView btnCancel;
    private TextView twMin;
    private TextView twMax;
    private TextView twTimeCutAudio;
    private ImageView btnPlay;
    private MediaPlayer mediaPlayer;
    private LineChart chart;
    private final Handler handler = new Handler();

    private int duration;
    private int percentMin = 0;
    private int percentMax = 100;
    private byte[] waveform;
    ArrayList<Float> wf;
    private List<Entry> entries;
//    String audioFilePath = "/storage/emulated/0/Music/Samsung/Over_the_Horizon.mp3";
    String audioFilePath = "/storage/emulated/0/Recordings/139-40 Đ. Trần Hưng Đạo (2).mp3";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_cut);
        entries = new ArrayList<>();

        Intent intent = getIntent();
        audioFilePath = intent.getStringExtra("PATH_KEY");

        twMin = findViewById(R.id.tw_min);
        twMax = findViewById(R.id.tw_max);
        twTimeCutAudio = findViewById(R.id.tw_time_cut_audio);
        btnPlay = findViewById(R.id.btn_play_cut);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(view -> this.onBackPressed());

        nameCut = findViewById(R.id.name_recording_cut);
        String[] nameFile = audioFilePath.split("/");
        nameCut.setText(nameFile[nameFile.length-1]);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) v;
                if (imageView.getTag() == null || imageView.getTag().equals("ic_play")) {
                    imageView.setImageResource(R.drawable.ic_pause);
                    imageView.setTag("ic_pause");

                    mediaPlayer.start();
                } else {
                    imageView.setImageResource(R.drawable.ic_play);
                    imageView.setTag("ic_play");

                    mediaPlayer.pause();
                }
            }
        });

        btnBack = findViewById(R.id.btn_back_cut);
        btnBack.setOnClickListener(view -> this.onBackPressed());

        // Khởi tạo MediaPlayer và Visualizer
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        duration = mediaPlayer.getDuration();

        // Khởi tạo biểu đồ tần số âm thanh
        chart = findViewById(R.id.chart);
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

        wf = new ArrayList<Float>();
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

        // RANGE SEEKBAR
        RangeSeekBar<Integer> rangeSeekBar = findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setRangeValues(0, 100);
        twMin.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
        twMax.setText(String.format(Locale.getDefault(), "%02d:%02d", (duration / 1000) / 60, (duration/1000) % 60));
        twTimeCutAudio.setText(String.format(Locale.getDefault(), "%02d:%02d", (duration / 1000) / 60, (duration/1000) % 60));

        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // Handle the range seek bar values changed event
                percentMin = minValue;
                percentMax = maxValue;
                int minSecond = (int) ((Integer) minValue / 100f * (duration / 1000));
                int maxSecond = (int) ((Integer) maxValue / 100f * (duration / 1000));
                twMin.setText(String.format(Locale.getDefault(), "%02d:%02d", minSecond / 60, minSecond % 60));
                twMax.setText(String.format(Locale.getDefault(), "%02d:%02d", maxSecond / 60, maxSecond % 60));

                updateChart();

                int currentPosition = (int) (minValue / 100f * duration);
                mediaPlayer.seekTo(currentPosition);

                int delta = (int) ((percentMax - percentMin) / 100f * duration);
                twTimeCutAudio.setText(String.format(Locale.getDefault(), "%02d:%02d", (delta / 1000) / 60, (delta/1000) % 60));
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
            btnPlay.setImageResource(R.drawable.ic_play);
            btnPlay.setTag("ic_play");
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int currentPosition = mediaPlayer.getCurrentPosition();
                            // do something with currentPosition
                            chart.highlightValue((int) (currentPosition * (waveform.length * 1f / (duration - 2.2))), 0);
                            handler.postDelayed(this, 10); // update every 1 second

                            if (currentPosition > percentMax / 100f * duration) {
                                mediaPlayer.pause();
                                mediaPlayer.seekTo((int) (percentMin / 100f * duration));

                                btnPlay.setImageResource(R.drawable.ic_play);
                                btnPlay.setTag("ic_play");
                            }
                        }
                    }
                });
            }
        });

//        CUT
//        String inputFilePath = "/storage/emulated/0/Music/Samsung/Over_the_Horizon.mp3";
//        String outputFilePath = "/storage/emulated/0/Music/Samsung/Over_the_Horizon_cut.mp3";
//        String startTime = "00:00:30";
//        String duration = "00:00:10";
//
//        String[] cmd = {"-ss", startTime, "-t", duration, "-i", inputFilePath, outputFilePath};
//
//        FFmpeg fFmpeg = FFmpeg.getInstance(getBaseContext());
//        try {
//            fFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
//                @Override
//                public void onStart() {
//                    Log.e(TAG, "onStart");
//                }
//
//                @Override
//                public void onProgress(String message) {
//                    Log.e(TAG, "onProgress: " + message);
//                }
//
//                @Override
//                public void onFailure(String message) {
//                    Log.e(TAG, "onFailure: " + message);
//                }
//
//                @Override
//                public void onSuccess(String message) {
//                    Log.e(TAG, "onSuccess: " + message);
//                }
//
//                @Override
//                public void onFinish() {
//                    Log.e(TAG, "onFinish");
//                }
//            });
//        } catch (FFmpegCommandAlreadyRunningException e) {
//            throw new RuntimeException(e);
//        }
    }

//    private void updateChart() {
//        int curMinWave = (int) (wf.size() * (percentMin / 100f));
//        int curMaxWave = (int) (wf.size() * (percentMax / 100f));
//        Log.e(TAG, "updateChart: " + curMinWave + " " + curMaxWave);
//
//        entries.clear();
//        for (int i=curMinWave; i<curMaxWave; i = i + 67) {
//            if (Math.abs(wf.get(i)) < 60)
//                entries.add(new Entry(i, wf.get(i) / 10));
//            else if (Math.abs(wf.get(i)) < 80)
//                entries.add(new Entry(i, wf.get(i) / 6));
//            else if (Math.abs(wf.get(i)) < 90)
//                entries.add(new Entry(i, wf.get(i) / 4));
//            else if (Math.abs(wf.get(i)) < 116)
//                entries.add(new Entry(i, wf.get(i) / 3));
//            else
//                entries.add(new Entry(i, wf.get(i) / 2));
//        }
//        Log.e(TAG, "entries: " + entries.size());
//
//        int range = entries.size() * 2 > 9000 ? 9000 : entries.size() * 2;
//        chart.setMinimumWidth(range);
//
//        LineDataSet dataSet = new LineDataSet(entries, "");
//        dataSet.setDrawValues(false);
//        dataSet.setDrawCircles(false);
//        dataSet.setColor(Color.BLACK);
//        dataSet.setLineWidth(1f);
//        dataSet.setHighlightLineWidth(2f);
//        dataSet.setDrawHorizontalHighlightIndicator(false);
//        LineData lineData = new LineData(dataSet);
//        chart.setData(lineData);
//        chart.invalidate();
//
//        chart.highlightValue(0, 0);
//    }


    private void updateChart() {
        int curMinWave = (int) (waveform.length * (percentMin / 100f));
        int curMaxWave = (int) (waveform.length * (percentMax / 100f));
        Log.e(TAG, "updateChart: " + curMinWave + " " + curMaxWave);

        entries.clear();
        for (int i=curMinWave; i<curMaxWave; i = i + 134) {
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

        int range = entries.size() * 2 > 9000 ? 9000 : entries.size() * 2;
        chart.setMinimumWidth(range);

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
    protected void onDestroy() {
        super.onDestroy();

        // Dừng phát âm thanh và giải phóng tài nguyên
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}