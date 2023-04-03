package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReplayActivity  extends AppCompatActivity {
    MediaPlayer mediaPlayer = new MediaPlayer();
    int currentSongIndex = 0;
    private int currentPosition = 0;
    private long timeWhenPaused = 0;
    private long elapsedTime = 0;
    private long elapsedTimeSpeedUp = 0;
    private File[] files;
    private boolean flagPlaying = true, flagSpeed = false, flagRepeat = false; //Dừng
    private TextView txtNameReplay, txtTimeTotal;
    private Chronometer txtTimeCur;
    private ImageButton btnPlayReplay, btnPrevReplay, btnNextReplay, btnBackWard, btnForward, btnRepeat;
    private Button btnSpeed;
    private SeekBar skbarReplay;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);
        getSupportActionBar().hide();
        getFiles();
        BoundView();
        playCurrentSong();
    }
    public void BoundView(){
        txtNameReplay = (TextView) findViewById(R.id.txt_name_replay);
        txtTimeCur = (Chronometer) findViewById(R.id.timecurrent_replay);
        txtTimeTotal = (TextView) findViewById(R.id.timetotal_replay);
        btnPlayReplay = (ImageButton) findViewById(R.id.btn_play_replay);
        btnPrevReplay = (ImageButton) findViewById(R.id.btn_prev_replay);
        btnNextReplay = (ImageButton) findViewById(R.id.btn_next_replay);
        btnBackWard = (ImageButton) findViewById(R.id.btn_pr5_replay);
        btnForward = (ImageButton) findViewById(R.id.btn_next5_replay);
        btnSpeed = (Button) findViewById(R.id.btn_speed_replay);
        btnRepeat = (ImageButton) findViewById(R.id.btn_repeat_replay);
        skbarReplay = (SeekBar) findViewById(R.id.skbar_replay);

        //SEEKBAR
        skbarReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newPosition = (mediaPlayer.getDuration() * progress) / seekBar.getMax();
                    mediaPlayer.seekTo(newPosition);
                    txtTimeCur.setBase(SystemClock.elapsedRealtime() - mediaPlayer.getCurrentPosition());
                    txtTimeCur.start();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(!flagPlaying)
                {
                    btnPlayReplay.setImageResource(R.drawable.ic_play_replay);
                }
                mediaPlayer.pause();
                txtTimeCur.stop();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!flagPlaying)
                {
                    btnPlayReplay.setImageResource(R.drawable.ic_pause_replay);
                }
                mediaPlayer.start();
                txtTimeCur.start();

            }
        });

        skbarReplay.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    int width = skbarReplay.getWidth();
                    float newX = event.getX();
                    float percent = newX / width;
                    int newProgress = (int) (percent * skbarReplay.getMax());
                    skbarReplay.setProgress(newProgress);
                    return true;
                }
                return false;
            }
        });

        //CHRONOMETER
        txtTimeCur.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (!flagSpeed) {
                    elapsedTime = SystemClock.elapsedRealtime() - chronometer.getBase() + elapsedTimeSpeedUp;
                } else {
                    elapsedTimeSpeedUp = (SystemClock.elapsedRealtime() - chronometer.getBase()) * 2 - elapsedTime;
                    elapsedTime = (SystemClock.elapsedRealtime() - chronometer.getBase()) * 2;
                    long minutes = (elapsedTime / 1000) / 60;
                    long seconds = (elapsedTime / 1000) % 60;
                    String timeWhenSpeed = String.format("%02d:%02d", minutes, seconds);
                    txtTimeCur.setText(timeWhenSpeed);
                }
                float progress = elapsedTime / (float) mediaPlayer.getDuration();
                skbarReplay.setProgress((int) (progress * skbarReplay.getMax()));
            }
        });

        //PLAY
        btnPlayReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        //INCREASE SPEED
        btnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagRepeat = !flagRepeat;
                if(flagRepeat)
                {
                    Toast.makeText(getApplicationContext(), "Chế độ lặp lại", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Bỏ lặp lại", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnPrevReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSongIndex > 0) {
                    currentSongIndex--;
                    playCurrentSong();
                }
            }
        });
        btnNextReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSongIndex < files.length - 1) {
                    currentSongIndex++;
                    playCurrentSong();
                }
            }
        });
        btnBackWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int backwardTime = 5000; // 5 giây
                long timeBase = 0;

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
            }
        });
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int forwardTime = 5000; // 5 giây
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
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(flagRepeat)
                {
                    currentSongIndex = currentPosition;
                    playCurrentSong();
                }
                else{
                    if (currentSongIndex < files.length - 1) {
                        currentSongIndex++;
                        playCurrentSong();
                    }
                    else {
                        currentSongIndex = 0;
                        playCurrentSong();
                    }
                }
            }
        });
    }

    private void playCurrentSong() {
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
         files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && (file.getName().endsWith(".mp3") || file.getName().endsWith(".aac"));
            }
        });

    }
    public void changeLayoutFromReplay(View view){
        switch (view.getId()) {
            case R.id.replay_btn_adjust: {
                Intent intent = new Intent(this, AdjustActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_back_replay: {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
