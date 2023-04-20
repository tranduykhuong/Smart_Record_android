package com.devapp.smartrecord.editmenu.harmonic;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.devapp.smartrecord.editmenu.combine.CombineAudioAdapter;
import com.devapp.smartrecord.ui.home.Audio;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HarmonicActivity extends AppCompatActivity implements HarmonicModalAdapter.OnItemClickListener{
    private boolean flagPlaying = false, flagDelete = true, flagChoose = false;
    private int cntBtn = 0,  currentPosition = 0;
    private RecyclerView rcvHarmonicAudio;
    private CombineAudioAdapter harmonicAudioAdapter;
    private List<Audio> audioList, listAddFile;
    private String pathSound, fileName, finalName, deleteName, fileExit;
    private TextView txtName, txtCurTime, txtDurationTime;
    private Button btnHarmonic, btnCancel;
    private ImageButton btnPlay, btnAddFile;
    private File[] files;
    private final MediaPlayer mediaPlayer  = new MediaPlayer();
    private SeekBar seekBar;
    private File outputFile;
    private final String tempPath = Environment.getExternalStorageDirectory().toString() + "/Recordings/";
    private final Handler handler = new Handler();
    private File recordingsDirectory;
    private boolean[] checkedArray;
    private Runnable mRunnable;

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
        setContentView(R.layout.activity_harmonic);
        Objects.requireNonNull(getSupportActionBar()).hide();

        //RECEIVE PATH TỪ MENU
        Intent intent = getIntent();
        pathSound = intent.getStringExtra("PATH_KEY");
        BoundView();

        //SET FILE DEFAULT
        fileName = pathSound.substring(pathSound.lastIndexOf("/") + 1);
        File file = new File(tempPath + fileName);
        String name = file.getName();
        fileExit = name.substring(name.lastIndexOf("."));
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String fileSize = decimalFormat.format(1.0 * file.length() / (1024 * 1024));
        audioList.add(new Audio(name, formatTime(file), fileSize + " MB", "26/02/2023 10:11", R.drawable.ic_play_audio_item));

        //ADD SOUND
        finalName = "HM_" + fileName;
        txtName.setText(finalName);
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
        else {
            deleteName = finalName;
        }

        //SET DEFAULT
        CheckColor();
        seekBar.setProgress(0);
        btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        txtCurTime.setText("00:00:00");

        try {
            mediaPlayer.setDataSource(tempPath + fileName);
            mediaPlayer.prepare();
            txtDurationTime.setText(formatTime(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        btnPlay.setOnClickListener(view -> {
            flagPlaying = !flagPlaying;
            if(cntBtn == 0 && flagPlaying)
            {
                btnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
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

        btnAddFile.setOnClickListener(view -> {
            flagChoose = true;
            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.modal_add_file_combine, null);

            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT ;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

            TextView btnDestroyNote = popupView.findViewById(R.id.combine_modal_destroy);
            TextView btnOkRecordNote = popupView.findViewById(R.id.combine_modal_add);
            RecyclerView modalList = popupView.findViewById(R.id.listView_show_note);

            recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/");
            if (recordingsDirectory.exists()) {
                files = recordingsDirectory.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        if (file1.isFile() && file1.getName().endsWith(fileExit)) {
                            String fileName = file1.getName();
                            String fileSize1 = decimalFormat.format(1.0 * file1.length() / (1024 * 1024));
                            listAddFile.add(new Audio(fileName, formatTime(file1), fileSize1 + " MB", "", R.drawable.ic_play_audio_item));
                        }
                    }
                }
            }

            if (files != null) {
                checkedArray = new boolean[files.length];
            }

            HarmonicModalAdapter harmonicModalAdapter = new HarmonicModalAdapter(popupView.getContext(), this);
            harmonicModalAdapter.setData(listAddFile);

            modalList.setLayoutManager(new LinearLayoutManager(popupView.getContext()));
            modalList.setAdapter(harmonicModalAdapter);

            btnDestroyNote.setOnClickListener(v -> popupWindow.dismiss());

            btnOkRecordNote.setOnClickListener(view1 -> {
                if(fileExit.equals(".mp3"))
                {
                    for(int i = 0; i < checkedArray.length; i++)
                    {
                        if(checkedArray[i])
                        {
                            addFileSound(listAddFile.get(i).getName());
                            break;
                        }
                    }

                    ResetAdapter();


                    //MIX
                    MixAudio();

                    //XÉT LẠI MEDIA PLAYER
                    try {
                        mediaPlayer.reset();
                        //mediaPlayer.setDataSource(tempPath + finalName);
                        mediaPlayer.setDataSource(outputFile.getAbsolutePath());
                        mediaPlayer.prepare();
                        txtDurationTime.setText(formatTime(outputFile));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    CheckOnceAdd();
                }
                else {
                    popupView.getContext().getString(R.string.only_mp3);
                }

                popupWindow.setOnDismissListener(() -> Toast.makeText(view1.getContext(), popupView.getContext().getString(R.string.add_successfull), Toast.LENGTH_SHORT).show());

                popupWindow.dismiss();
            });

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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        txtCurTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 200);
                    }
                };
                handler.post(mRunnable);
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            seekBar.setProgress(0);
            txtCurTime.setText("00:00:00");
            flagPlaying = false;
            cntBtn = 0;
            btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });

        btnHarmonic.setOnClickListener(view -> ConfirmCombine());

        btnCancel.setOnClickListener(view -> {
            if (audioList.size() > 1) {
                audioList = audioList.subList(0, 1);
            }

            harmonicAudioAdapter.notifyDataSetChanged();
            harmonicAudioAdapter.setData(audioList);
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

            flagChoose = false;
        });
    }

    public void CheckColor(){
        if(audioList.size() > 1)
        {
            btnCancel.setEnabled(true);
            @SuppressLint("Recycle") TypedArray typedArray = obtainStyledAttributes(new int[]{R.attr.textColor});
            int textColor = typedArray.getColor(0, 0);
            btnCancel.setTextColor(textColor);
            btnHarmonic.setEnabled(true);
            btnHarmonic.setTextColor(ContextCompat.getColor(this, R.color.pink_500));
        }
        else {
            btnCancel.setEnabled(false);
            btnHarmonic.setEnabled(false);
            btnCancel.setTextColor(Color.GRAY);
            btnHarmonic.setTextColor(Color.GRAY);
        }
    }
    private void ConfirmCombine(){
        AlertDialog.Builder alertDiaglog = new AlertDialog.Builder(this);
        alertDiaglog.setTitle(this.getString(R.string.harmonic_title));
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage(this.getString(R.string.harmonic_YN));
        alertDiaglog.setPositiveButton(this.getString(R.string.harmonic_title), (dialogInterface, i) -> {
            Toast.makeText(getApplicationContext(), this.getString(R.string.harmonic_success), Toast.LENGTH_LONG).show();
            flagDelete = false;
            finish();
        });
        alertDiaglog.setNegativeButton(this.getString(R.string.cancel_announce), (dialogInterface, i) -> {
            File fileDl = new File(tempPath + deleteName);
            File fileDel = new File(String.valueOf(fileDl.getAbsoluteFile()));
            if (fileDel.exists()) {
                fileDel.delete();
                Toast.makeText(getApplicationContext(), this.getString(R.string.cancel_announce), Toast.LENGTH_LONG).show();
            }

            onBackPressed();
        });

        alertDiaglog.show();
    }
    public void BoundView(){
        txtName = findViewById(R.id.harmonic_txt_name);
        txtCurTime = findViewById(R.id.harmonic_time_current);
        txtDurationTime = findViewById(R.id.harmonic_time_total);
        rcvHarmonicAudio = findViewById(R.id.harmonic_rcv_audio_list);
        rcvHarmonicAudio.setLayoutManager(new LinearLayoutManager(this));
        audioList = new ArrayList<>();
        harmonicAudioAdapter = new CombineAudioAdapter(this);
        harmonicAudioAdapter.setData(audioList);
        rcvHarmonicAudio.setAdapter(harmonicAudioAdapter);
        seekBar = findViewById(R.id.harmonic_seekbar);
        btnPlay = findViewById(R.id.harmonic_btn_play);
        btnHarmonic = findViewById(R.id.harmonic_btn_harmonic);
        btnCancel = findViewById(R.id.harmonic_btn_cancel);
        btnAddFile = findViewById(R.id.harmonic_add_file);
        listAddFile = new ArrayList<>();
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
        mediaPlayer.stop();
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
    protected void onStop() {
        super.onStop();
    }
    @Override
    public void onItemClick(int position) {
        if(checkedArray[position])
        {
            checkedArray[position] = false;
        }
        else {
            checkedArray[position] = true;
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void ResetAdapter(){
        harmonicAudioAdapter.notifyDataSetChanged();
        harmonicAudioAdapter.setData(audioList);
        seekBar.setProgress(0);
        btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        txtCurTime.setText("00:00:00");
        CheckColor();
    }

    @SuppressLint("DefaultLocale")
    public void MixAudio() {
        List<String> inputPaths = new ArrayList<>();
        for (Audio audio : audioList) {
            File ifi = new File(tempPath + audio.getName());
            inputPaths.add(ifi.getAbsolutePath());
        }

        if(audioList.size() > 1)
        {
            String[] command = new String[] {"-i", inputPaths.get(0), "-i", inputPaths.get(1), "-filter_complex", "[0:a]amix=inputs=2:duration=longest:dropout_transition=2[aout]", "-map", "[aout]", "-f", "mp3", "-acodec", "libmp3lame", "-ab", "128k", "-ar", "44100", "-ac", "2", outputFile.getAbsolutePath()};

            int rc = FFmpeg.execute(command);
            if (rc != RETURN_CODE_SUCCESS) {
                throw new RuntimeException("Fmpeg command failed with exit code " + rc);
            }
        }
    }


    public void CheckOnceAdd()
    {
        if(flagChoose)
        {
            btnAddFile.setOnClickListener(view -> {
                Toast.makeText(this, this.getString(R.string.add_file_announce), Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void changeLayoutFromHarmonic(View view) {
        if (view.getId() == R.id.harmonic_btn_back) {
            onBackPressed();
        }
    }
}
