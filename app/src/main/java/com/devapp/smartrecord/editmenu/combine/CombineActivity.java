package com.devapp.smartrecord.editmenu.combine;

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
import com.devapp.smartrecord.ui.home.Audio;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class CombineActivity extends AppCompatActivity implements CombineModalAdapter.OnItemClickListener{
    private boolean flagPlaying = false, flagDelete = true, flagChoose = false;
    private int cntBtn = 0,  currentPosition = 0;
    private RecyclerView rcvCombineAudio;
    private CombineAudioAdapter combineAudioAdapter;
    private List<Audio> audioList, listAddFile;
    private String pathSound, fileName, finalName, deleteName, fileExit;
    private TextView txtName, txtCurTime, txtDurationTime;
    private Button btnCombine, btnCancel;
    private ImageButton btnPlay, btnAddFile;
    private int size;
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
        audioList.add(new Audio(tmpName, formatTime(tmpFile), fileSize + " MB", "26/02/2023 10:11", R.drawable.ic_play_outline));
    }

    @SuppressLint({"WrongConstant", "DefaultLocale", "SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine);
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
        audioList.add(new Audio(name, formatTime(file), fileSize + " MB", "26/02/2023 10:11", R.drawable.ic_play_outline));

        //ADD SOUND
        finalName = "CB_" + fileName;
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
            if (recordingsDirectory.listFiles() != null)
                size = Objects.requireNonNull(recordingsDirectory.listFiles()).length;
            if (recordingsDirectory.exists()) {
                files = recordingsDirectory.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        if (file1.isFile() && file1.getName().endsWith(fileExit)) {
                            String fileName = file1.getName();
                            String fileSize1 = decimalFormat.format(1.0 * file1.length() / (1024 * 1024));
                            listAddFile.add(new Audio(fileName, formatTime(file1), fileSize1 + " MB", "", R.drawable.ic_play_outline));
                        }
                    }
                }
            }

            if (files != null) {
                checkedArray = new boolean[files.length];
            }

            CombineModalAdapter combineModalAdapter = new CombineModalAdapter(popupView.getContext(), this);
            combineModalAdapter.setData(listAddFile);

            modalList.setLayoutManager(new LinearLayoutManager(popupView.getContext()));
            modalList.setAdapter(combineModalAdapter);

            btnDestroyNote.setOnClickListener(v -> popupWindow.dismiss());

            btnOkRecordNote.setOnClickListener(view1 -> {
                if(!checkAllFalse(checkedArray))
                {
                    if(fileExit.equals(".mp3"))
                    {
                        for(int i = 0; i < checkedArray.length; i++)
                        {
                            if(checkedArray[i])
                            {
                                addFileSound(listAddFile.get(i).getName());
                            }
                        }

                        ResetAdapter();

                        //COMBINE
                        ConcatAudio();

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
                }
                else {
                    Toast.makeText(popupView.getContext(), popupView.getContext().getString(R.string.announce_no_file_add), Toast.LENGTH_SHORT).show();
                }

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
                cntBtn = 0;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(tempPath + fileName);
                mediaPlayer.prepare();
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
        alertDiaglog.setTitle(this.getString(R.string.combine_title));
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage(this.getString(R.string.combine_YN));
        alertDiaglog.setPositiveButton(this.getString(R.string.combine_title), (dialogInterface, i) -> {
            Toast.makeText(getApplicationContext(), this.getString(R.string.combine_success), Toast.LENGTH_LONG).show();
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
        btnAddFile = findViewById(R.id.combine_add_file);
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

    public void changeLayoutFromCombine(View view) {
        if (view.getId() == R.id.combine_btn_back) {
            onBackPressed();
        }
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
        combineAudioAdapter.notifyDataSetChanged();
        combineAudioAdapter.setData(audioList);
        seekBar.setProgress(0);
        btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        txtCurTime.setText("00:00:00");
        CheckColor();
    }
    @SuppressLint("DefaultLocale")
    public void ConcatAudio(){
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
                            command = new String[]{"-i", in1.getAbsolutePath(), "-i", in2.getAbsolutePath(), "-filter_complex", String.format(filter, 2), "-f", "mp3", "-acodec", "libmp3lame", "-ab", "128k", "-ar", "44100", "-ac", "2", outputFile.getAbsolutePath()};
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
    }

    public void CheckOnceAdd() {
        if(flagChoose)
        {
            btnAddFile.setOnClickListener(view -> {
                Toast.makeText(this, this.getString(R.string.add_file_announce), Toast.LENGTH_SHORT).show();
            });
        }
    }

    public boolean checkAllFalse(boolean[] checkedArray){
        boolean all_false = true;
        for(int i = 0; i< checkedArray.length; i++)
        {
            if(checkedArray[i] == true){
                all_false = false;
                break;
            }
        }
        return all_false;
    }

}
