package com.devapp.smartrecord.soundvariation;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.HomeAudioAdapter;

import org.apache.commons.io.FilenameUtils;

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
    private ImageButton variationBtnSave;
    private boolean isPlay = false;
    private final int currPosition = 0;
    private int currentPosition = 0;
    private ImageView btnBack;
    private long timeWhenPaused = 0;
    private String fileName;
    private File destinationFile, destinationFolder;
    private Context context;
    private Handler handler = new Handler();
    private Runnable mRunnable;

    // Constructor không tham số
    public VariationActivity() {
    }
    public VariationActivity(Context context) {
        this.context = context;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_sound_variation);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("file_name");


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

    private void changeAudioFrequency(String inputFilePath, String outputFilePath, String tempWavFilePath, String tempModifiedWavFilePath, int newFrequency) {

        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);

        // Kiểm tra nếu tệp đầu vào không tồn tại
        if (!inputFile.exists()) {
            return;
        }

        // Kiểm tra nếu tệp đầu ra đã tồn tại
        if (outputFile.exists()) {
            outputFile.delete();
        }
        String[] cmd1 = {
                "-i",
                inputFilePath,
                "-vn",
                "-acodec",
                "pcm_s16le",
                "-ar",
                "44100",
                "-ac",
                "2",
                tempWavFilePath
        };

        // Thực thi lệnh FFmpeg
        int rc1 = FFmpeg.execute(cmd1);
        if (rc1 == RETURN_CODE_SUCCESS) {
            /// Lệnh FFmpeg để thay đổi tần số của tệp WAV
            String[] cmd2 = {
                    "-i",
                    tempWavFilePath,
                    "-af",
                    "volume=2.0",
                    "-filter:a",
                    "asetrate=" + newFrequency + ",atempo=1",
                    tempModifiedWavFilePath
            };

            // Thực thi lệnh FFmpeg
            int rc2 = FFmpeg.execute(cmd2);
            if (rc2 == RETURN_CODE_SUCCESS) {
                // Lệnh FFmpeg để chuyển đổi tệp WAV được thay đổi tần số sang đầu ra
                String[] cmd3 = {
                        "-i",
                        tempModifiedWavFilePath,
                        "-vn",
                        "-ar",
                        String.valueOf(newFrequency),
                        "-ac",
                        "2",
                        outputFilePath
                };

                // Thực thi lệnh FFmpeg
                int rc3 = FFmpeg.execute(cmd3);
                if (rc3 == RETURN_CODE_SUCCESS) {
                    // Quá trình chuyển đổi tần số thành công
                    Log.e("Src", "ok");
                } else {
                    // Quá trình chuyển đổi tần số không thành công
                    Log.e("Src", "!ok");

                }
            } else {
                Log.e("Src", "!!ok");

            }
        } else {
            // Quá trình chuyển đổi tần số không thành công
        }

        // Xóa tệp WAV tạm thời đầu tiên
        File tempWavFile = new File(tempWavFilePath);
        if (tempWavFile.exists()) {
            boolean deleted = tempWavFile.delete();
            if (deleted) {
                System.out.println("Đã xóa tệp WAV tạm thời đầu tiên.");
            } else {
                System.out.println("Không thể xóa tệp WAV tạm thời đầu tiên.");
            }
        }

        // Xóa tệp WAV tạm thời thứ hai
        File tempModifiedWavFile = new File(tempModifiedWavFilePath);
        if (tempModifiedWavFile.exists()) {
            boolean deleted = tempModifiedWavFile.delete();
            if (deleted) {
                System.out.println("Đã xóa tệp WAV tạm thời thứ hai.");
            } else {
                System.out.println("Không thể xóa tệp WAV tạm thời thứ hai.");
            }
        }
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
        variationBtnSave = this.findViewById(R.id.variation_btn_save);

        btnBack = findViewById(R.id.variation_back);
        btnBack.setOnClickListener(view -> this.onBackPressed());

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
                if(destinationFile != null) {
                    destinationFile.delete();
                    onBackPressed();
                }
                else {
                    onBackPressed();
                }

            }
        });

        variationAvatarViewNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cntBtn = 0;
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
                    mediaPlayer.prepare();
                    seekBarTime.setMax(mediaPlayer.getDuration());
                    btnPlay.setImageResource(R.drawable.ic_play_combine_main);
                    variationAvatarViewNone.setEnabled(false);
                    variationAvatarImageRobot.setEnabled(true);
                    variationAvatarImageChild.setEnabled(true);
                    isPlay = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        variationAvatarImageRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sourceFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/");
                destinationFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/TmpAudio/");

                // Tạo thư mục thùng rác nếu chưa tồn tại
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdir();
                }

                cntBtn = 0;

                File sourceFile = new File(sourceFolder, fileName); // Đường dẫn tệp âm thanh gốc
                destinationFile = new File(destinationFolder, "Robot " + fileName); // Đường dẫn tệp âm thanh đích
                File tempWavFilePath = new File(destinationFolder, "tmp" + FilenameUtils.removeExtension(fileName) + ".wav");
                File tempModifiedWavFilePath = new File(destinationFolder, "tmpModified" + FilenameUtils.removeExtension(fileName) + ".wav");
                changeAudioFrequency(String.valueOf(sourceFile), String.valueOf(destinationFile), String.valueOf(tempWavFilePath),
                        String.valueOf(tempModifiedWavFilePath),32000);

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(destinationFile.getAbsolutePath());
                    mediaPlayer.prepare();
                    seekBarTime.setMax(mediaPlayer.getDuration());
                    btnPlay.setImageResource(R.drawable.ic_play_combine_main);
                    variationAvatarViewNone.setEnabled(true);
                    variationAvatarImageRobot.setEnabled(false);
                    variationAvatarImageChild.setEnabled(true);
                    isPlay = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        variationAvatarImageChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sourceFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/");
                destinationFolder = new File(Environment.getExternalStorageDirectory() + "/Recordings/TmpAudio/");

                // Tạo thư mục thùng rác nếu chưa tồn tại
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdir();
                }

                cntBtn = 0;

                File sourceFile = new File(sourceFolder, fileName); // Đường dẫn tệp âm thanh gốc
                destinationFile = new File(destinationFolder, "Child " + fileName); // Đường dẫn tệp âm thanh đích
                File tempWavFilePath1 = new File(destinationFolder, "tmpChild" + FilenameUtils.removeExtension(fileName) + ".wav");
                File tempModifiedWavFilePath1 = new File(destinationFolder, "tmpModifiedChild" + FilenameUtils.removeExtension(fileName) + ".wav");
                changeAudioFrequency(String.valueOf(sourceFile), String.valueOf(destinationFile), String.valueOf(tempWavFilePath1),
                        String.valueOf(tempModifiedWavFilePath1), 48000);

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(destinationFile.getAbsolutePath());
                    mediaPlayer.prepare();
                    seekBarTime.setMax(mediaPlayer.getDuration());
                    btnPlay.setImageResource(R.drawable.ic_play_combine_main);
                    variationAvatarViewNone.setEnabled(true);
                    variationAvatarImageRobot.setEnabled(true);
                    variationAvatarImageChild.setEnabled(false);
                    isPlay = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        variationBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sourceFile  = new File(String.valueOf(destinationFile)); // Lấy đường dẫn đầy đủ đến tệp
                File destinationFolderHome = new File(Environment.getExternalStorageDirectory() + "/Recordings/");
                String nameAudio = "Robot " + fileName;
                if (!destinationFolderHome.exists()) {
                    destinationFolderHome.mkdir();
                }
                //Tạo ra dialog để xác nhận xóa hay không
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(view.getContext().getString(R.string.question_save));
                builder.setPositiveButton(view.getContext().getString(R.string.answer_yes), (dialogInterface, j) -> {
                    try {
                        if (sourceFile.exists()) { //Kiểm tra tệp có tồn tại hay không
                            File destinationFile = new File(destinationFolderHome, nameAudio); // Tạo tệp đích mới
                            if (sourceFile.renameTo(destinationFile)) {// Di chuyển tệp đến thư mục đích và kiểm tra kết quả
                                Log.d("TAG", "Lưu tệp âm thanh thành công");
                                Toast.makeText(context, view.getContext().getString(R.string.announce_save_successfully), Toast.LENGTH_SHORT).show();
                                sourceFile.delete(); // Xóa tệp trong thư mục gốc sau khi chuyển thành công
                            } else {
                                Log.d("TAG", "Lưu tệp âm thanh không thành công");
                            }
                        }
                        else {
                            Log.d("TAG", "Tệp không tồn tại");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                builder.setNegativeButton(view.getContext().getString(R.string.answer_no), (dialogInterface, i) -> {

                });
                builder.show();
            }
        });

        seekBarTime.setProgress(0);
        btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        currTime.setText("00:00:00");

        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            mediaPlayer.prepare();
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            timeMax.setText(formatTime(file));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Khi bấm nút play
        btnPlay.setOnClickListener(view -> {
            isPlay = !isPlay;
            if(cntBtn == 0 && isPlay)
            {
                btnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.start();
                seekBarTime.setMax(mediaPlayer.getDuration());
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

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBarTime.setProgress(currentPosition);
                        currTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 200);
                    }
                };
                handler.post(mRunnable);
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            seekBarTime.setProgress(0);
            currTime.setText("00:00:00");
            isPlay = false;
            cntBtn = 0;
            btnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });
    }

    private void playCurrentAudio(){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
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
        handler.removeCallbacks(mRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
