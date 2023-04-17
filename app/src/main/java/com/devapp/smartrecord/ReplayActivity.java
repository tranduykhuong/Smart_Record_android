package com.devapp.smartrecord;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.api.VoiceToTextActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ReplayActivity  extends AppCompatActivity {
    MediaPlayer mediaPlayer = new MediaPlayer();
    int currentSongIndex = 0;
    private int currentPosition, duration;
    private long timeWhenPaused = 0;
    private long elapsedTime = 0;
    private long elapsedTimeSpeedUp = 0;
    private File[] files;
    private boolean flagPlaying = true, flagSpeed = false, flagRepeat = false; //Stop
    private TextView txtNameReplay, txtTimeTotal;
    private HorizontalScrollView hrzScrollView;
    private Chronometer txtTimeCur;
    private ImageButton btnPlayReplay;
    private Button btnSpeed;
    private SeekBar skbarReplay;
    private LineChart chart;
    ArrayList<Float> wf;
    private byte[] waveform;
    private List<Entry> entries;
    private final Handler handler = new Handler();
    private boolean isSeekBarTouched = false;
    private int progressWidth;
    private int realWidth;
    private float rate1, rate2, rate3;
    private Runnable highlight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);
        Objects.requireNonNull(getSupportActionBar()).hide();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        progressWidth = displayMetrics.widthPixels;
        realWidth = displayMetrics.widthPixels;

        getFiles();
        entries = new ArrayList<>();

        Intent intent = getIntent();
        String action = intent.getAction();
        String nameSound;
        
        if(action.equals("FromHome"))
        {
            nameSound = intent.getStringExtra("Name");
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].getName().equals(nameSound))
                {
                    currentSongIndex = i;
                    break;
                }
            }
        }
        else if(action.equals("FromTrash"))
        {
            nameSound = intent.getStringExtra("NameTrash");
            File trashDirectory = new File(Environment.getExternalStorageDirectory() + "/TrashAudio");
            files = trashDirectory.listFiles(file -> file.isFile() && (file.getName().endsWith(".mp3") || file.getName().endsWith(".aac")));
            for(int i = 0; i < Objects.requireNonNull(files).length; i++)
            {
                if(files[i].getName().equals(nameSound))
                {
                    currentSongIndex = i;
                    break;
                }
            }
        }

        BoundView();
        playCurrentSong(currentSongIndex);

        getHistoryNote();
    }

    public void getHistoryNote(){
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String json = sharedPreferences.getString(files[currentSongIndex].getName(), null);

        if (json != null) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(json);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String note = jsonArray.getString(i);
                    Log.d("note: ", note);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void BoundView(){
        txtNameReplay =  findViewById(R.id.txt_name_replay);
        txtTimeCur =  findViewById(R.id.timecurrent_replay);
        txtTimeTotal =  findViewById(R.id.timetotal_replay);
        btnPlayReplay =  findViewById(R.id.btn_play_replay);
        btnSpeed =  findViewById(R.id.btn_speed_replay);
        skbarReplay =  findViewById(R.id.skbar_replay);
        hrzScrollView = findViewById(R.id.replay_horizontal);
        ImageButton btnPrevReplay = findViewById(R.id.btn_prev_replay);
        ImageButton btnNextReplay = findViewById(R.id.btn_next_replay);
        ImageButton btnBackWard = findViewById(R.id.btn_pr5_replay);
        ImageButton btnForward = findViewById(R.id.btn_next5_replay);
        ImageButton btnRepeat = findViewById(R.id.btn_repeat_replay);

        // Khởi tạo biểu đồ tần số âm thanh
        chart = findViewById(R.id.replay_chart);
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
//        File fileWave = new File(files[currentSongIndex].getAbsolutePath());
        File fileWave = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/" + files[currentSongIndex].getName());
        byte[] data = new byte[(int) fileWave.length()];
        try (FileInputStream inputStream = new FileInputStream(fileWave)) {
            inputStream.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        IntBuffer intBuffer = buffer.asIntBuffer();

//        wf = new ArrayList<>();
//        while (intBuffer.hasRemaining()) {
//            int sample = intBuffer.get();
//            float amplitude = (float) (Math.abs((float) sample) / 32768.0);
//            // sử dụng cường độ âm thanh tại mỗi mẫu dữ liệu ở đây
//            wf.add(amplitude);
//        }

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

        // Cập nhật vẽ chart
        updateChart();

        //SEEKBAR
        skbarReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isSeekBarTouched = true;
                    int newPosition = (mediaPlayer.getDuration() * progress) / seekBar.getMax();
                    mediaPlayer.seekTo(newPosition);
                    isSeekBarTouched = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouched = true;
                mediaPlayer.pause();
                txtTimeCur.stop();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newPosition = (mediaPlayer.getDuration() * seekBar.getProgress()) / seekBar.getMax();
                mediaPlayer.seekTo(newPosition);
                mediaPlayer.start();
                txtTimeCur.setBase(SystemClock.elapsedRealtime() - mediaPlayer.getCurrentPosition());
                txtTimeCur.start();
                isSeekBarTouched = false;
            }
        });

        //CHRONOMETER
        txtTimeCur.setOnChronometerTickListener(chronometer -> {
            if (!isSeekBarTouched) {
                if (!flagSpeed) {
                    elapsedTime = SystemClock.elapsedRealtime() - chronometer.getBase() + elapsedTimeSpeedUp;
                } else {
                    elapsedTimeSpeedUp = (SystemClock.elapsedRealtime() - chronometer.getBase()) * 2 - elapsedTime;
                    elapsedTime = (SystemClock.elapsedRealtime() - chronometer.getBase()) * 2;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime));
                    @SuppressLint("DefaultLocale") String timeWhenSpeed = String.format("%02d:%02d", minutes, seconds);
                    txtTimeCur.setText(timeWhenSpeed);
                }
                float progress = elapsedTime / (float) mediaPlayer.getDuration();
                skbarReplay.setProgress((int) (progress * skbarReplay.getMax()));
            }
        });

        //PLAY
        btnPlayReplay.setOnClickListener(view -> {
            flagPlaying = !flagPlaying;
            if (!flagPlaying) {
                btnPlayReplay.setImageResource(R.drawable.ic_play_replay);
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();

                timeWhenPaused = SystemClock.elapsedRealtime() - txtTimeCur.getBase();
                txtTimeCur.stop();
            } else {
                btnPlayReplay.setImageResource(R.drawable.ic_pause_replay);
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();

                txtTimeCur.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
                txtTimeCur.start();
            }
        });

        //INCREASE SPEED
        btnSpeed.setOnClickListener(view -> {
            flagSpeed = !flagSpeed;
            txtTimeCur.stop();
            if (flagSpeed) {
                btnSpeed.setText("x2");
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(2.0f));
                txtTimeCur.setBase(SystemClock.elapsedRealtime() - elapsedTime / 2);
            }
            else{
                btnSpeed.setText("x1");
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1.0f));
                txtTimeCur.setBase(SystemClock.elapsedRealtime() - elapsedTime);
            }
            txtTimeCur.start();
        });

        btnRepeat.setOnClickListener(view -> {
            flagRepeat = !flagRepeat;
            if(flagRepeat)
            {
                Toast.makeText(getApplicationContext(), "Chế độ lặp lại", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Bỏ lặp lại", Toast.LENGTH_LONG).show();
            }
        });
        btnPrevReplay.setOnClickListener(view -> {
            if (currentSongIndex > 0) {
                currentSongIndex--;
                playCurrentSong(currentSongIndex);
            }
            else if(currentSongIndex == 0)
            {
                currentSongIndex = files.length - 1;
                playCurrentSong(currentSongIndex);
            }
        });
        btnNextReplay.setOnClickListener(view -> {
            if (currentSongIndex < files.length - 1) {
                currentSongIndex++;
                playCurrentSong(currentSongIndex);
            }
            else if(currentSongIndex == files.length - 1)
            {
                currentSongIndex = 0;
                playCurrentSong(currentSongIndex);
            }
        });
        btnBackWard.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int backwardTime = 5000; // 5 seconds
            long timeBase;

            if (currentPosition - backwardTime > 0) {
                if(flagPlaying)
                {
                    mediaPlayer.seekTo(currentPosition - backwardTime);
                    timeBase = txtTimeCur.getBase() + (long)backwardTime;
                    txtTimeCur.setBase(timeBase);
                    txtTimeCur.start();
                }

            } else {
                mediaPlayer.seekTo(0);
                txtTimeCur.setBase(SystemClock.elapsedRealtime());
                txtTimeCur.start();
            }
        });
        btnForward.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int forwardTime = 5000; // 5 seconds
            int duration = mediaPlayer.getDuration();

            if (currentPosition + forwardTime < duration) {
                mediaPlayer.seekTo(currentPosition + forwardTime);
                long timeBase = txtTimeCur.getBase() - (long)forwardTime;
                txtTimeCur.setBase(timeBase);
                txtTimeCur.start();
            } else {
                mediaPlayer.seekTo(duration);
                txtTimeCur.setBase(duration);
                txtTimeCur.stop();
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if(flagRepeat)
            {
                currentSongIndex = currentPosition;
                playCurrentSong(currentSongIndex);
            }
            else{
                if (currentSongIndex < files.length - 1) {
                    currentSongIndex++;
                    playCurrentSong(currentSongIndex);
                }
                else {
                    currentSongIndex = 0;
                    playCurrentSong(currentSongIndex);
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                highlight = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int crtPosition = mediaPlayer.getCurrentPosition();
                            chart.highlightValue((int) (crtPosition * (waveform.length * 1f / (duration - 2.2))), 0);

                            rate1 = 1.0f * realWidth / chart.getWidth();
                            rate2 = 1.0f * crtPosition / duration;

                            if (rate3 > 0.01 && rate2 > rate3)
                            {
                                rate3 = rate3 + rate1;
                                hrzScrollView.smoothScrollTo(progressWidth, 0);
                                progressWidth = progressWidth + realWidth;
                            }

                            handler.postDelayed(this, 10);
                        }
                    }
                };
                handler.post(highlight);
            }
        });

    }

    private void updateChart() {
        int percentMin = 0;
        int curMinWave = (int) (waveform.length * (percentMin / 100f));
        int percentMax = 100;
        int curMaxWave = (int) (waveform.length * (percentMax / 100f));

        if(entries != null)
        {
            entries.clear();
        }

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

        assert entries != null;
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

    @SuppressLint("SetTextI18n")
    private void playCurrentSong(int currentSongIndex) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(files[currentSongIndex].getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            duration = mediaPlayer.getDuration();
            flagPlaying = true;
            flagSpeed = false;
            btnSpeed.setText("x1");
            txtNameReplay.setText(files[currentSongIndex].getName());
            skbarReplay.setProgress(0);
            skbarReplay.setMax(duration);
            btnPlayReplay.setImageResource(R.drawable.ic_pause_replay);
            txtTimeCur.setBase(SystemClock.elapsedRealtime());
            txtTimeCur.start();
            txtTimeTotal.setText(getTotalTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getTotalTime() {
        int totalTime = mediaPlayer.getDuration();
        return getTimeString(totalTime);
    }

    private String getTimeString(int millis) {
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millis);
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(minutes));
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void getFiles(){
        File directory = new File(Environment.getExternalStorageDirectory() + "/Recordings");
        files = directory.listFiles(file -> file.isFile() && (file.getName().endsWith(".mp3") || file.getName().endsWith(".aac") || file.getName().endsWith(".m4a")));

    }
    @SuppressLint("NonConstantResourceId")
    public void changeLayoutFromReplay(View view){
        mediaPlayer.stop();
        txtTimeCur.stop();

        switch (view.getId()) {
            case R.id.replay_btn_adjust: {
                File file = new File(Environment.getExternalStorageDirectory().toString()+ "/Recordings/" + files[currentSongIndex].getName());
                Intent intent1 = new Intent(this, EditMenuActivity.class);
                intent1.putExtra("PATH_KEY", file.getAbsolutePath());
                startActivity(intent1);
                break;
            }
            case R.id.btn_back_replay: {
                finish();
                break;
            }
            case R.id.replay_btn_text: {
                Intent intent = new Intent(this, VoiceToTextActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        finish();
    }

    @Override
    protected void onDestroy() {
        mediaPlayer.stop();
        handler.removeCallbacks(highlight);
        super.onDestroy();
    }
}
