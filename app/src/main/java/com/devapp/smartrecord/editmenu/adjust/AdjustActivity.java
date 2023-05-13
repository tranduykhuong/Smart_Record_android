package com.devapp.smartrecord.editmenu.adjust;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaFormat;
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

import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.EditMenuActivity;
import com.devapp.smartrecord.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdjustActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private PlaybackParams params;

    private Chronometer currTime;
    private ImageButton btnPlay;
    private TextView timeMax;
    private boolean isPlay = true, isDone = false, isExist = false, isSave = false;
    private SeekBar adjustSeekBarSpeed, adjustSeekBarVolume, adjustSeekBarFoolproof;
    private int currPosition = 0;
    private int currentPosition = 0;
    private long timeWhenPaused = 0;
    private String pathSound;
    private Handler mainHandler;
    private Runnable updateSeekBarRunnable;
    private ArrayList<String> listAudioAdjusted;
    private String addListAdjusted = "";
    private Gson gson = new Gson();
    private String nameFile;
    private int position = 0, foolProofInit, volumeInit, speedInit;
    private final String tempPath = Environment.getExternalStorageDirectory().toString() + "/Recordings/";
    private File outputFile;
    private float volume, foolProof, speed;

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

        nameFile = pathSound.substring(pathSound.lastIndexOf("/") + 1);
        titleFile.setText(nameFile);

        SharedPreferences adjustMemories = getSharedPreferences("adjustMemories", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = adjustMemories.edit();

        String json = adjustMemories.getString("listAdjusted", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            listAudioAdjusted = gson.fromJson(json, type);

            for (int i = 0; i < listAudioAdjusted.size(); i++) {
                String name = listAudioAdjusted.get(i).split("\\|")[0];

                if (nameFile.equalsIgnoreCase(name)) {
                    isDone = true;
                    isExist = true;
                    position = i;
                    foolProofInit = Integer.parseInt(listAudioAdjusted.get(i).split("\\|")[1]);
                    volumeInit = Integer.parseInt(listAudioAdjusted.get(i).split("\\|")[2]);
                    speedInit = Integer.parseInt(listAudioAdjusted.get(i).split("\\|")[3]);

                    adjustSeekBarFoolproof.setProgress(foolProofInit);
                    adjustSeekBarVolume.setProgress(volumeInit);
                    adjustSeekBarSpeed.setProgress(speedInit);
                }
            }
            if (!isDone){
                adjustSeekBarFoolproof.setProgress(0);
                adjustSeekBarVolume.setProgress(0);
                adjustSeekBarSpeed.setProgress(0);
            }
        }
        else{
            listAudioAdjusted = new ArrayList<>();
            adjustSeekBarFoolproof.setProgress(0);
            adjustSeekBarVolume.setProgress(0);
            adjustSeekBarSpeed.setProgress(0);
        }

        adjustSeekBarFoolproof.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        foolProof = (float) progress / seekBar.getMax() + 0.25f;
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
                btnPlay.setImageResource(R.drawable.ic_play_adjust);
                mediaPlayer.pause();
                currTime.stop();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                btnPlay.setImageResource(R.drawable.ic_play_adjust);
                mediaPlayer.pause();
                currTime.stop();
            }
        });

        adjustSeekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        if (fromUser) {
                            volume = (float) progress / seekBar.getMax();
                            if (volume <= -0.5f) volume = -0.5f;
                            mediaPlayer.setVolume(0.5f + volume, 0.5f + volume);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                btnPlay.setImageResource(R.drawable.ic_play_adjust);
                mediaPlayer.pause();
                currTime.stop();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                btnPlay.setImageResource(R.drawable.ic_play_adjust);
                mediaPlayer.pause();
                currTime.stop();
            }
        });

        adjustSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        if (progress == 1){
                            params.setSpeed(2f);
                            speed = 2f;
                        }
                        else if (progress == 2){
                            params.setSpeed(3f);
                            speed = 3f;
                        }
                        else if (progress == -1){
                            params.setSpeed(0.5f);
                            speed = 0.5f;
                        }
                        else if (progress == -2){
                            params.setSpeed(0.25f);
                            speed = 0.25f;
                        }
                        else {
                            params.setSpeed(1f);
                            speed = 1f;
                        }
                        mediaPlayer.setPlaybackParams(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                btnPlay.setImageResource(R.drawable.ic_play_adjust);
                mediaPlayer.pause();
                currTime.stop();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                btnPlay.setImageResource(R.drawable.ic_play_adjust);
                mediaPlayer.pause();
                currTime.stop();
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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
                if (isExist){
                    addListAdjusted += nameFile + "|";
                    addListAdjusted += adjustSeekBarFoolproof.getProgress() + "|";
                    addListAdjusted += adjustSeekBarVolume.getProgress() + "|";
                    addListAdjusted += adjustSeekBarSpeed.getProgress();
                    listAudioAdjusted.set(position, addListAdjusted);
                }
                else{
                    addListAdjusted += nameFile + "|";
                    addListAdjusted += adjustSeekBarFoolproof.getProgress() + "|";
                    addListAdjusted += adjustSeekBarVolume.getProgress() + "|";
                    addListAdjusted += adjustSeekBarSpeed.getProgress();
                    listAudioAdjusted.add(addListAdjusted);
                }

                outputFile = new File(tempPath, "Adjust_" + nameFile);
                String speedValue = String.format("atempo=" + speed).replace(",", ".");
                String volumeValue = String.format("volume="+ (volume + 0.5f)).replace(",", ".");
                String foolProofValue = String.format("asetrate=44100*"+ (foolProof + 1f)).replace(",", ".");
                String valueFormat = speedValue + ", " + volumeValue + ", " + foolProofValue;
                Log.d("name", valueFormat);
//                String[] command = {"-i", pathSound, "-filter:a", String.format("atempo=%.2f, volume=%.2f, asetrate=%.2f*44100", speed, volume, foolProof), "-f", "mp3", outputFile.getAbsolutePath()};
                String[] command = {"-i", pathSound, "-filter:a", valueFormat, "-f", "mp3", outputFile.getAbsolutePath()};

                int rc = FFmpeg.execute(command);

                String json = gson.toJson(listAudioAdjusted);
                editor.putString("listAdjusted", json);
                editor.commit();

                Toast.makeText(AdjustActivity.this, getText(R.string.announce_save_successfully), Toast.LENGTH_SHORT).show();
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

            if (isExist){
                volume = 0.5f + (float) volumeInit/adjustSeekBarVolume.getMax();
                if (volume <= -0.5f) volume = -0.5f;

                foolProof = 0.25f + (float) foolProofInit/adjustSeekBarFoolproof.getMax();
                if (foolProof < -0.5f) foolProof = -0.5f;

                if (speedInit == 1){
                    params.setSpeed(2f);
                    speed = 2f;
                }
                else if (speedInit == 2){
                    params.setSpeed(3f);
                    speed = 3f;
                }
                else if (speedInit == -1){
                    params.setSpeed(0.5f);
                    speed = 0.5f;
                }
                else if (speedInit == -2){
                    params.setSpeed(0.25f);
                    speed = 0.25f;
                }
                else {
                    params.setSpeed(1f);
                    speed = 1f;
                }

                mediaPlayer.setVolume(0.5f + volume, 0.5f + volume);
                params.setPitch(1f + foolProof);
                params.setSpeed(speed);
                mediaPlayer.setPlaybackParams(params);
            }
            else{
                mediaPlayer.setVolume(0.5f, 0.5f);
                params.setPitch(1f);
                mediaPlayer.setPlaybackParams(params);
            }

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
