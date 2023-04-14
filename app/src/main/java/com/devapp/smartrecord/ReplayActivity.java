package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.api.VoiceToTextActivity;
import com.devapp.smartrecord.services.RecordingService;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.suman.voice.graphviewlibrary.GraphView;
import com.suman.voice.graphviewlibrary.WaveSample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ReplayActivity  extends AppCompatActivity {
    MediaPlayer mediaPlayer = new MediaPlayer();
    int currentSongIndex = 0;
    private int currentPosition;
    private long timeWhenPaused = 0;
    private long elapsedTime = 0;
    private long elapsedTimeSpeedUp = 0;
    private File[] files;
    private boolean flagPlaying = true, flagSpeed = false, flagRepeat = false; //Stop
    private TextView txtNameReplay, txtTimeTotal;
    private Chronometer txtTimeCur;
    private ImageButton btnPlayReplay;
    private Button btnSpeed;
    private SeekBar skbarReplay;
    private boolean isSeekBarTouched = false;
    private GraphView graphView;
    private final List<WaveSample> pointList = new ArrayList<>();
    private Thread mRecordingThread;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getFiles();
        BoundView();

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
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].getName().equals(nameSound))
                {
                    currentSongIndex = i;
                    break;
                }
            }
        }

        playCurrentSong(currentSongIndex);
    }
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void BoundView(){
        txtNameReplay =  findViewById(R.id.txt_name_replay);
        txtTimeCur =  findViewById(R.id.timecurrent_replay);
        txtTimeTotal =  findViewById(R.id.timetotal_replay);
        btnPlayReplay =  findViewById(R.id.btn_play_replay);
        btnSpeed =  findViewById(R.id.btn_speed_replay);
        skbarReplay =  findViewById(R.id.skbar_replay);
        ImageButton btnPrevReplay = findViewById(R.id.btn_prev_replay);
        ImageButton btnNextReplay = findViewById(R.id.btn_next_replay);
        ImageButton btnBackWard = findViewById(R.id.btn_pr5_replay);
        ImageButton btnForward = findViewById(R.id.btn_next5_replay);
        ImageButton btnRepeat = findViewById(R.id.btn_repeat_replay);
        graphView = findViewById(R.id.graphView);
        graphView.setGraphColor(Color.rgb(18, 17, 17));
        graphView.setCanvasColor(Color.rgb(255, 255, 255));
        graphView.setTimeColor(Color.rgb(0, 0, 0));
        graphView.setWaveLengthPX(13);


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

    }

    @SuppressLint("SetTextI18n")
    private void playCurrentSong(int currentSongIndex) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(files[currentSongIndex].getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            flagPlaying = true;
            flagSpeed = false;
            btnSpeed.setText("x1");
            txtNameReplay.setText(files[currentSongIndex].getName());
            skbarReplay.setProgress(0);
            skbarReplay.setMax(mediaPlayer.getDuration());
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
                File file = new File(getApplicationContext().getFilesDir(), files[currentSongIndex].getName());
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
}
