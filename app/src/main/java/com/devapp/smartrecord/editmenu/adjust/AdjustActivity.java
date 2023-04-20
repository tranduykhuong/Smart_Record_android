package com.devapp.smartrecord.editmenu.adjust;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.devapp.smartrecord.EditMenuActivity;
import com.devapp.smartrecord.R;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdjustActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private PlaybackParams params;

    private Chronometer currTime;
    private ImageButton btnPlay;
    private TextView timeMax;
    private boolean isPlay = true;
    private SeekBar adjustSeekBarSpeed, adjustSeekBarVolume, adjustSeekBarFoolproof;
    private int currPosition = 0;
    private int currentPosition = 0;
    private long timeWhenPaused = 0;
    private String pathSound;
    private Handler mainHandler;
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        pathSound = intent.getStringExtra("PATH_KEY");
        boundView();
        playCurrentAudio();
    }

    private void boundView(){
        ImageButton btnBack = (ImageButton) this.findViewById(R.id.adjust_back);
        TextView titleFile = (TextView) this.findViewById(R.id.adjust_title);
        TextView btnDestroyAdjust = (TextView) this.findViewById(R.id.adjust_destroy);
        TextView bntAdjust = (TextView) this.findViewById(R.id.adjust_btn);
        currTime = (Chronometer) this.findViewById(R.id.adjust_timecurrent);
        timeMax = (TextView) this.findViewById(R.id.adjust_timetotal);
        btnPlay = (ImageButton) this.findViewById(R.id.adiust_btn_play);
        SeekBar seekBarTime = (SeekBar) this.findViewById(R.id.adjust_seek_bar);

        adjustSeekBarFoolproof = this.findViewById(R.id.adjust_seekbar_foolproof);
        adjustSeekBarVolume = this.findViewById(R.id.adjust_seekbar_volume);
        adjustSeekBarSpeed = this.findViewById(R.id.adjust_seekbar_speed);

        titleFile.setText(pathSound.substring(pathSound.lastIndexOf("/") + 1));

        SharedPreferences adjustMemories = getSharedPreferences("adjustMemories", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = adjustMemories.edit();
        boolean isFirstTime = adjustMemories.getBoolean("isFirstTime", true);

        if (isFirstTime){
            adjustSeekBarFoolproof.setProgress(0);
            adjustSeekBarVolume.setProgress(0);
            adjustSeekBarSpeed.setProgress(0);
        }
        else{
            adjustSeekBarFoolproof.setProgress(adjustMemories.getInt("adjustFoolproof", 0));
            adjustSeekBarVolume.setProgress(adjustMemories.getInt("adjustVolume", 0));
            adjustSeekBarSpeed.setProgress(adjustMemories.getInt("adjustSpeed", 0));
        }


        adjustSeekBarFoolproof.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        float foolProof = (float) progress / seekBar.getMax();
                        if (foolProof < -0.5f) foolProof = -0.5f;
                        params.setPitch(1f + foolProof); // Điều chỉnh cao độ
                        mediaPlayer.setPlaybackParams(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        adjustSeekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        if (fromUser) {
                            float volume = (float) progress / seekBar.getMax();
                            if (volume < -0.5f) volume = -0.5f;
                            mediaPlayer.setVolume(0.5f + volume, 0.5f + volume);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        adjustSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        if (progress == 1){
                            params.setSpeed(2f);
                        }
                        else if (progress == 2){
                            params.setSpeed(3f);
                        }
                        else if (progress == -1){
                            params.setSpeed(0.5f);
                        }
                        else if (progress == -2){
                            params.setSpeed(0.25f);
                        }
                        else {
                            params.setSpeed(1f);
                        }
                        mediaPlayer.setPlaybackParams(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newPosition = (mediaPlayer.getDuration() * progress) / seekBar.getMax();
                    mediaPlayer.seekTo(newPosition);
                    currTime.setBase(SystemClock.elapsedRealtime() - mediaPlayer.getCurrentPosition());

                    if (!isPlay){
                        currTime.stop();
                    }
                    else {
                        currTime.start();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(!isPlay)
                {
                    btnPlay.setImageResource(R.drawable.ic_play_adjust);
                    mediaPlayer.pause();
                    currTime.stop();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!isPlay)
                {
                    btnPlay.setImageResource(R.drawable.ic_adjust_replay);
                    mediaPlayer.start();
                    currTime.start();
                }
            }
        });

        mainHandler = new Handler(Looper.getMainLooper());
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int totalDuration = mediaPlayer.getDuration();
                    float percentage = ((float) currentPosition / totalDuration) * 100;
                    seekBarTime.setProgress((int) percentage);
                    new Handler(Looper.getMainLooper()).postDelayed(this, 100);

                    if (percentage > 99.3) {
                        currTime.stop();
                        mediaPlayer.stop();
                        btnPlay.setImageResource(R.drawable.ic_play_adjust);
                        // Hủy bỏ Runnable khi đạt đến cuối phát nhạc hoặc video
                        mainHandler.removeCallbacks(this);
                    } else {
                        // Tiếp tục cập nhật thanh trượt sau 100ms
//                    mainHandler.postDelayed(this, 100);
                    }
                }
            }
        };

        seekBarTime.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    int width = seekBarTime.getWidth();
                    float newX = event.getX();
                    float percent = newX / width;
                    int newProgress = (int) (percent * seekBarTime.getMax());
                    seekBarTime.setProgress(newProgress);
                    return true;
                }
                return false;
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlay = !isPlay;
                if (isPlay){
                    btnPlay.setImageResource(R.drawable.ic_play_adjust);
                    currentPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();

                    timeWhenPaused = SystemClock.elapsedRealtime() - currTime.getBase();
                    currTime.stop();
                }
                else{
                    btnPlay.setImageResource(R.drawable.ic_adjust_replay);
                    mediaPlayer.seekTo(currentPosition);
                    mediaPlayer.start();
                    currTime.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
                    currTime.start();
                    mainHandler.post(updateSeekBarRunnable);
                }
            }
        });

        btnDestroyAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainHandler.removeCallbacksAndMessages(null);
                    onBackPressed();
            }
        });

        bntAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isFirstTime", false);
                editor.putInt("adjustFoolproof", adjustSeekBarFoolproof.getProgress());
                editor.putInt("adjustVolume", adjustSeekBarVolume.getProgress());
                editor.putInt("adjustSpeed", adjustSeekBarSpeed.getProgress());
                editor.commit();

                mediaPlayer.stop();
                mediaPlayer.release();
                onBackPressed();
            }
        });
    }

    private void playCurrentAudio(){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(pathSound);
            currTime.setBase(SystemClock.elapsedRealtime());
            mediaPlayer.prepare();
            currTime.stop();
            timeMax.setText(getTotalTime());
            params = mediaPlayer.getPlaybackParams();
            mediaPlayer.setVolume(0.5f, 0.5f);
            params.setPitch(1f);
            mediaPlayer.setPlaybackParams(params);
            mediaPlayer.pause();
        } catch (IOException e) {
            Log.e("MediaPlayer", "prepare() failed");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
