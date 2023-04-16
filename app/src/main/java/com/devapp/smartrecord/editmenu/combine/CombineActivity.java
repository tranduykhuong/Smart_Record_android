package com.devapp.smartrecord.editmenu.combine;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class CombineActivity extends AppCompatActivity {
    private boolean flagPlaying = false, flagDelete = true;
    private int cntBtn = 0,  currentPosition = 0;
    private RecyclerView rcvCombineAudio;
    private CombineAudioAdapter combineAudioAdapter;
    private List<Audio> audioList;
    private String pathSound, fileName, finalName, deleteName;
    private TextView txtName, txtCurTime, txtDurationTime;
    private Button btnCombine, btnCancel;
    private ImageButton btnPlay;
    private final MediaPlayer mediaPlayer  = new MediaPlayer();
    private SeekBar seekBar;
    private File outputFile;
    private final String tempPath = Environment.getExternalStorageDirectory().toString() + "/Recordings/";
    private final Handler handler = new Handler();

    public void addFileSound(String FName)
    {
        File tmpFile = new File(tempPath + FName);
        String tmpName = tmpFile.getName();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String fileSize = decimalFormat.format(1.0 * tmpFile.length() / (1024 * 1024));
        audioList.add(new Audio(tmpName, formatTime(tmpFile), fileSize + " MB", "26/02/2023 10:11", R.drawable.ic_play_audio_item));
    }

    @SuppressLint({"WrongConstant", "DefaultLocale", "SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine);
        Objects.requireNonNull(getSupportActionBar()).hide();

        //NHẬN PATH TỪ MENU
        Intent intent = getIntent();
        pathSound = intent.getStringExtra("PATH_KEY");
        BoundView();

        //GÁN FILE MẶC ĐỊNH
        fileName = pathSound.substring(pathSound.lastIndexOf("/") + 1);
        File file = new File(tempPath + fileName);
        String name = file.getName();
        String fileExit = name.substring(name.lastIndexOf("."));
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String fileSize = decimalFormat.format(1.0 * file.length() / (1024 * 1024));
        audioList.add(new Audio(name, formatTime(file), fileSize + " MB", "26/02/2023 10:11", R.drawable.ic_play_audio_item));

        //ADD SOUND
//        addFileSound("Record (12).mp3");

        finalName = "CB_" + fileName;
        txtName.setText(finalName);

        //COMBINE LIST AUDIO

        outputFile = new File(tempPath + finalName);
        if(outputFile.exists()) {
            int i = 1;
            while (outputFile.exists())
            {
                deleteName = finalName.substring(0, finalName.lastIndexOf(".")) + " (" + i + ")" + fileExit;
                outputFile = new File(tempPath + finalName.substring(0, finalName.lastIndexOf(".")) + " (" + i + ")" + fileExit);
                i++;
            }
        }

        int rc;
        String[] command = null;
        String filter = "concat=n=%d:v=0:a=1";
        int count = 0;

        List<String> inputPaths = new ArrayList<>();
        for (Audio audio : audioList) {
            File ifi = new File(tempPath + audio.getName());
            inputPaths.add(ifi.getAbsolutePath());
        }

        if(audioList.size() > 1)
        {
            while (inputPaths.size() > 0) {
                int numInputs = inputPaths.size();
                if (numInputs == 1) {
                    try {
                        FileUtils.copyFile(new File(inputPaths.get(0)), outputFile);
                        count++;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                } else if (numInputs == 2) {
                    File in1 = new File(inputPaths.get(0));
                    File in2 = new File(inputPaths.get(1));
                    command = new String[]{"-i", in1.getAbsolutePath(), "-i", in2.getAbsolutePath(), "-filter_complex", String.format(filter, 2), "-f", "mp3", "-acodec", "libmp3lame", "-ab", "128k", "-ar", "44100", "-ac", "2", outputFile.getAbsolutePath()};
                    rc = FFmpeg.execute(command);
                    if (rc == RETURN_CODE_SUCCESS) {
                        count++;
                    }
                    break;
                } else {
                    List<String> outputPaths = new ArrayList<>();
                    for (int i = 0; i < numInputs; i += 2) {
                        File in1 = new File(inputPaths.get(i));
                        File in2;
                        if (i + 1 < numInputs) {
                            in2 = new File(inputPaths.get(i + 1));
                            String outputName = String.format("%sconcat_%d.mp3", tempPath, i / 2);
                            outputPaths.add(outputName);
                            command = new String[]{"-i", in1.getAbsolutePath(), "-i", in2.getAbsolutePath(), "-filter_complex", String.format(filter, 2), "-f", fileExit.substring(1), "-acodec", "libmp3lame", "-ab", "128k", "-ar", "44100", "-ac", "2", outputName};
                        } else {
                            outputPaths.add(inputPaths.get(i));
                        }
                        rc = FFmpeg.execute(command);
                        if (rc == RETURN_CODE_SUCCESS) {
                            count++;
                        }
                    }
                    inputPaths = outputPaths;
                }
            }

            if (count == 0) {
                throw new RuntimeException("No input files to concat.");
            }

            File[] filesToDelete = new File(tempPath).listFiles((dir, named) -> named.matches("concat_.*\\.mp3"));
            if (filesToDelete != null) {
                for (File files : filesToDelete) {
                    files.delete();
                }
            }
        }

        //XÉT MẶC ĐỊNH MỚI VÀO
        CheckColor();
        seekBar.setProgress(0);
        btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        txtCurTime.setText("00:00:00");

        try {
            if(audioList.size() > 1)
            {
                mediaPlayer.setDataSource(tempPath + finalName);
                txtDurationTime.setText(formatTime(outputFile));
            }
            else {
                mediaPlayer.setDataSource(tempPath + fileName);
                txtDurationTime.setText(formatTime(file));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        btnPlay.setOnClickListener(view -> {
            flagPlaying = !flagPlaying;
            if(cntBtn == 0 && flagPlaying)
            {
                try {
                    btnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                cntBtn = 1;
            }
            else if(cntBtn == 1 && !flagPlaying)
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
                    seekBar.setProgress(mCurrentPosition);
                    txtCurTime.setText(formatTime(mCurrentPosition));
                }
                // Lặp lại Runnable sau 100ms
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(mRunnable, 100);

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBar.setProgress(0);
            txtCurTime.setText("00:00:00");

            flagPlaying = false;
            cntBtn = 0;
            btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });

        btnCombine.setOnClickListener(view -> ConfirmCombine());

        btnCancel.setOnClickListener(view -> {
            if (audioList.size() > 1) {
                audioList = audioList.subList(0, 1);
            }

            combineAudioAdapter.notifyDataSetChanged();
            combineAudioAdapter.setData(audioList);
            seekBar.setProgress(0);
            btnPlay.setImageResource(R.drawable.ic_play_combine_main);
            txtCurTime.setText("00:00:00");
            CheckColor();

            try {
                mediaPlayer.stop();
                mediaPlayer.setDataSource(tempPath + fileName);
                txtDurationTime.setText(formatTime(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void CheckColor()
    {
        if(audioList.size() > 1)
        {
            btnCancel.setEnabled(true);
            @SuppressLint("Recycle") TypedArray typedArray = obtainStyledAttributes(new int[]{R.attr.textColor});
            int textColor = typedArray.getColor(0, 0);
            btnCancel.setTextColor(textColor);
            btnCombine.setEnabled(true);
            btnCombine.setTextColor(ContextCompat.getColor(this, R.color.pink_500));
        }
        else {
            btnCancel.setEnabled(false);
            btnCombine.setEnabled(false);
            btnCancel.setTextColor(Color.GRAY);
            btnCombine.setTextColor(Color.GRAY);
        }
    }
    private void ConfirmCombine(){
        AlertDialog.Builder alertDiaglog = new AlertDialog.Builder(this);
        alertDiaglog.setTitle("Combination");
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage("Do you want to combine?");
        alertDiaglog.setPositiveButton("Combine", (dialogInterface, i) -> {
            Toast.makeText(getApplicationContext(), "Successfull Combine", Toast.LENGTH_LONG).show();
            flagDelete = false;
            onBackPressed();
        });
        alertDiaglog.setNegativeButton("Cancel", (dialogInterface, i) -> {
            File fileDl = new File(tempPath + deleteName);
            File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
            if (fileDel.exists()) {
                fileDel.delete();
                Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_LONG).show();
            }

            onBackPressed();
        });

        alertDiaglog.show();
    }
    public void BoundView(){
        txtName = findViewById(R.id.combine_txt_name);
        txtCurTime = findViewById(R.id.combine_time_current);
        txtDurationTime = findViewById(R.id.combine_time_total);
        rcvCombineAudio = findViewById(R.id.combine_rcv_audio_list);
        rcvCombineAudio.setLayoutManager(new LinearLayoutManager(this));
        audioList = new ArrayList<>();
        combineAudioAdapter = new CombineAudioAdapter(this);
        combineAudioAdapter.setData(audioList);
        rcvCombineAudio.setAdapter(combineAudioAdapter);
        seekBar = findViewById(R.id.combine_seekbar);
        btnPlay = findViewById(R.id.combine_btn_play);
        btnCombine = findViewById(R.id.combine_btn_combine);
        btnCancel = findViewById(R.id.combine_btn_cancel);

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

    @Override
    protected void onDestroy() {
        if(flagDelete)
        {
            File fileDl = new File(tempPath + deleteName);
            File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
            if (fileDel.exists()) {
                fileDel.delete();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(flagDelete)
        {
            File fileDl = new File(tempPath + deleteName);
            File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
            if (fileDel.exists()) {
                fileDel.delete();
            }
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(flagDelete)
        {
            File fileDl = new File(tempPath + deleteName);
            File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
            if (fileDel.exists()) {
                fileDel.delete();
            }
        }
        super.onStop();
    }

    public void changeLayoutFromCombine(View view) {
        if (view.getId() == R.id.combine_btn_back) {
            onBackPressed();
        }
    }
}
