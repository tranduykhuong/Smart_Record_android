package com.devapp.smartrecord.soundvariation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.R;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VariationActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private PlaybackParams params;
    private int cntBtn = 0;
    private Chronometer currTime;
    private ImageView btnPlay;
    private TextView timeMax;
    private ImageView variationAvatarViewNone, variationAvatarImageRobot, variationAvatarImageChild;
    private boolean isPlay = false;
    private final int currPosition = 0;
    private int currentPosition = 0;
    private ImageView btnBack;
    private long timeWhenPaused = 0;
    private Handler handler = new Handler();

    public VariationActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_sound_variation);
//        playCurrentAudio();
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
        btnBack = this.findViewById(R.id.variation_back);
        currTime = this.findViewById(R.id.variation_timecurrent);
        timeMax = this.findViewById(R.id.variation_timetotal);
        btnPlay = this.findViewById(R.id.ic_play_circle_24);
        SeekBar seekBarTime = this.findViewById(R.id.variation_seek_bar);
        variationAvatarViewNone = this.findViewById(R.id.variation_avatar_image_view_none);
        variationAvatarImageRobot = this.findViewById(R.id.variation_avatar_image_view_robot);
        variationAvatarImageChild = this.findViewById(R.id.variation_avatar_image_view_child);


        SharedPreferences variationMemories = getSharedPreferences("variationMemories", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = variationMemories.edit();
        boolean isFirstTime = variationMemories.getBoolean("isFirstTime", true);

        if (isFirstTime){
        }
        else{

        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        seekBarTime.setProgress(0);
        btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        currTime.setText("00:00:00");

        try {

            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/vinh.aac");
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/vinh.aac");
            timeMax.setText(formatTime(file));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        btnPlay.setOnClickListener(view -> {
            isPlay = !isPlay;
            if(cntBtn == 0 && isPlay)
            {
                try {
                    btnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    seekBarTime.setMax(mediaPlayer.getDuration());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                cntBtn = 1;
            }
            else if(cntBtn == 1 && !isPlay)
            {
                btnPlay.setImageResource(R.drawable.ic_play_combine_main);
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
            else if(cntBtn == 1)
            {
                btnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
            }

        });

        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                currTime.setText(formatTime(progress));
                seekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Không làm gì khi bắt đầu kéo SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Không làm gì khi kết thúc kéo SeekBar
            }
        });

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition();
                    seekBarTime.setProgress(mCurrentPosition);
                    currTime.setText(formatTime(mCurrentPosition));
                }
                // Lặp lại Runnable sau 100ms
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(mRunnable, 100);

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBarTime.setProgress(0);
            currTime.setText("00:00:00");
            try {
                mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/vinh.aac");
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/vinh.aac");
                timeMax.setText(formatTime(file));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            isPlay = false;
            cntBtn = 0;
            btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });
    }

    private void playCurrentAudio(){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/vinh.aac");
//            mediaPlayer.prepare();
//            currTime.setBase(SystemClock.elapsedRealtime());
//            currTime.setBase(0);
//            currTime.stop();
//            timeMax.setText(getTotalTime());
//            params = mediaPlayer.getPlaybackParams();
//            mediaPlayer.setVolume(0.5f, 0.5f);
//            params.setPitch(1f);
//            mediaPlayer.setPlaybackParams(params);
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
