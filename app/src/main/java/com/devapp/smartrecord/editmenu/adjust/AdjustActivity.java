package com.devapp.smartrecord.editmenu.adjust;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.devapp.smartrecord.EditMenuActivity;
import com.devapp.smartrecord.R;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class AdjustActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPlay = false;
    private SeekBar adjustSeekBarSpeed, adjustSeekBarVolume, adjustSeekBarFoolproof;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust);
        getSupportActionBar().hide();

        TextView titleFile = (TextView) this.findViewById(R.id.adjust_title);
        TextView btnDestroyAdjust = (TextView) this.findViewById(R.id.adjust_destroy);
        TextView bntAdjust = (TextView) this.findViewById(R.id.adjust_btn);
        TextView timeMax = (TextView) this.findViewById(R.id.adjust_timetotal);
        ImageButton btnPlay = (ImageButton) this.findViewById(R.id.adiust_btn_play);
        ProgressBar progressBarTime = (ProgressBar) this.findViewById(R.id.adjust_progress_bar);

        adjustSeekBarFoolproof = this.findViewById(R.id.adjust_seekbar_foolproof);
        adjustSeekBarVolume = this.findViewById(R.id.adjust_seekbar_volume);
        adjustSeekBarSpeed = this.findViewById(R.id.adjust_seekbar_speed);

        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/Voice Recorder/Thoại 001.m4a");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        timeMax.setText(getFileDuration(mediaPlayer.getDuration()));


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
            adjustSeekBarVolume.setProgress(adjustMemories.getInt("adjustFoolproof", 0));
            adjustSeekBarSpeed.setProgress(adjustMemories.getInt("adjustSpeed", 0));
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlay){
                    isPlay = true;
                    btnPlay.setImageResource(R.drawable.ic_play_combine);

                    try {
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        mediaPlayer.setVolume(adjustSeekBarVolume.getProgress(), adjustSeekBarVolume.getProgress());

                        PlaybackParams params = mediaPlayer.getPlaybackParams();
                        params.setPitch(adjustSeekBarFoolproof.getProgress()); // Điều chỉnh cao độ
                        params.setSpeed(adjustSeekBarSpeed.getProgress()); // Điều chỉnh tốc độ phát lại thành 75% so với bình thường.
                        mediaPlayer.setPlaybackParams(params);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    mediaPlayer.pause();
                    isPlay = false;
                }
            }
        });

        btnDestroyAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isFirstTime", false);
                Intent intent = new Intent(AdjustActivity.this, EditMenuActivity.class);
                startActivity(intent);
            }
        });

        bntAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isFirstTime", false);
                editor.putInt("adjustFoolproof", adjustSeekBarFoolproof.getProgress());
                editor.putInt("adjustVolume", adjustSeekBarVolume.getProgress());
                editor.putInt("adjustSpeed", adjustSeekBarSpeed.getProgress());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private String getFileDuration(int duration) {
        int hours = duration / 3600000;
        int minutes = (duration % 3600000) / 60000;
        int seconds = ((duration % 3600000) % 60000) / 1000;


        String durationString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return durationString;
    }
}
