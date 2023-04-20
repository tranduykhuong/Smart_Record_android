package com.devapp.smartrecord.editmenu.divide;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.EditMenuActivity;
import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DivideActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int cntBtn = 0;
    private Chronometer currTime;
    private TextView timeMax;
    private boolean isPlay = false;
    private int currentPosition = 0;
    private final Handler handler = new Handler();
    private String fileName, pathSound;
    private TextView divideNameAudio;
    private ImageButton divideBtnPlay, divideBtn;
    private Button divideBtnConfirm;
    private File inputFile, outputFile1, outputFile2;
    private String divideTime, randomString;
    private int flagCnt = 0;
    String inputFilePath, outputFilePath;

    // Một ArrayList để lưu trữ các giá trị thời gian
    ArrayList<String> timeList = new ArrayList<>();

    private Context context;
    private Runnable mRunnable;
    private Executor executor = Executors.newFixedThreadPool(2);
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    public DivideActivity() {
    }

    public DivideActivity(Context context) {
        this.context = context;
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_divide);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        pathSound = intent.getStringExtra("PATH_KEY");
        fileName = pathSound.substring(pathSound.lastIndexOf("/") + 1);
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
        divideNameAudio = this.findViewById(R.id.divide_name_audio);
        divideBtnPlay = this.findViewById(R.id.divide_btn_play);
        timeMax = this.findViewById(R.id.divide_time_of_audio);
        currTime = this.findViewById(R.id.divide_current_time_audio);
        divideBtn = this.findViewById(R.id.divide_btn_action);
        divideBtnConfirm = this.findViewById(R.id.divide_btn_confirm);

        // Đường dẫn tệp MP3 đầu vào
        inputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName;
        outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/";
        Log.e("sdfjnsdjfnskdfmksdjnfksd", inputFilePath);
        Log.e("sdfjnsdjfnskdfmksdjnfksd", outputFilePath);
        randomString = generateRandomString(3);
        inputFile = new File(inputFilePath);
        outputFile1 = new File(outputFilePath, "divide1 " + FilenameUtils.removeExtension(fileName) + "_" + randomString + ".mp3");
        outputFile2 = new File(outputFilePath, "divide2 " + FilenameUtils.removeExtension(fileName) + "_" + randomString + ".mp3");

        divideNameAudio.setText(fileName);
        //Mở file chuẩn bị
        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            mediaPlayer.prepare();
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            timeMax.setText(formatTime(file));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Khi bấm nút play
        divideBtnPlay.setOnClickListener(view -> {
            isPlay = !isPlay;
            if(cntBtn == 0 && isPlay)
            {
                divideBtnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.start();
                cntBtn = 1;
            }
            else if(cntBtn == 1 && !isPlay)
            {
                divideBtnPlay.setImageResource(R.drawable.ic_play_combine_main);
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
            else if(cntBtn == 1)
            {
                divideBtnPlay.setImageResource(R.drawable.ic_pause_combine_main);
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
            }

        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        currTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 200);
                    }
                };
                handler.post(mRunnable);
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            currTime.setText("00:00:00");
            isPlay = false;
            cntBtn = 0;
            divideBtnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });
        divideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flagCnt == 0) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        // Lấy thời gian hiện tại của bản ghi âm thanh đang phát
                        String currentTime = formatTime(currentPosition);
                        timeList.add(currentTime);
                        divideTime = currentTime;
                        flagCnt = 1;
                        // Sử dụng thời gian hiện tại trong công việc cần thực hiện
                        Toast.makeText(getApplicationContext(), "Thời gian hiện tại: " + currentTime, Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "timeList: " + timeList.toString());
                    } else {
                        // Hiển thị thông báo cho người dùng rằng không có âm thanh đang phát
                        Toast.makeText(getApplicationContext(), "Không có âm thanh đang phát", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Chỉ được phép chia một lần!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Nhấn nút chia
        divideBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra xem các tệp con đã tồn tại chưa, nếu có thì tạo tên mới để tránh ghi đè lên tệp cũ
                if (outputFile1.exists()) {
                    // Nếu outputFile1 đã tồn tại, tạo tên khác ngẫu nhiên
                    String newOutputFileName1 = "divide1 " + FilenameUtils.removeExtension(fileName) + "_" + generateRandomString(3) + ".mp3";
                    outputFile1 = new File(outputFilePath, newOutputFileName1);
                }

                if (outputFile2.exists()) {
                    // Nếu outputFile2 đã tồn tại, tạo tên khác ngẫu nhiên
                    String newOutputFileName2 = "divide2 " + FilenameUtils.removeExtension(fileName) + "_" + generateRandomString(3) + ".mp3";
                    outputFile2 = new File(outputFilePath, newOutputFileName2);
                }

                String[] commandPart1 = {
                        "-i", String.valueOf(inputFile),
                        "-c:a", "mp3", // Mã codec âm thanh là mp3
                        "-t", divideTime, // Thời gian của phần 1 (đơn vị: giây)
                        outputFile1.getAbsolutePath()
                };

                String[] commandPart2 = {
                        "-i", String.valueOf(inputFile),
                        "-c:a", "mp3", // Mã codec âm thanh là mp3
                        "-ss", divideTime, // Thời gian bắt đầu của phần 2 (đơn vị: giây)
                        outputFile2.getAbsolutePath()
                };

                //Tạo ra dialog để xác nhận xóa hay không
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(view.getContext().getString(R.string.question_divide));
                builder.setPositiveButton(view.getContext().getString(R.string.answer_yes), (dialogInterface, j) -> {
                    try {
                        // Thực thi lệnh FFmpeg
                        int rc3 = FFmpeg.execute(commandPart1);
                        if (rc3 == RETURN_CODE_SUCCESS) {
                            // Quá trình chuyển đổi tần số thành công
                            Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.announce_divide_successfully), Toast.LENGTH_SHORT).show();
                            Log.e("Src", "ok");
                        } else {
                            // Quá trình chuyển đổi tần số không thành công
                            Log.e("Src", "!ok");
                        }
                        int rc4 = FFmpeg.execute(commandPart2);
                        if (rc4 == RETURN_CODE_SUCCESS) {
                            // Quá trình chuyển đổi tần số thành công
                            Log.e("Src", "ok");
                        } else {
                            // Quá trình chuyển đổi tần số không thành công
                            Log.e("Src", "!ok");
                        }
                        mediaPlayer.stop();
                        Intent intentBackHome = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intentBackHome);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                builder.setNegativeButton(view.getContext().getString(R.string.answer_no), (dialogInterface, i) -> {
                });
                builder.show();
            }
        });

    }//end bound view

    //Lấy kí tự ngẫu nhiên
    public static String generateRandomString(int length) {
        String randomUUID = UUID.randomUUID().toString();
        return randomUUID.substring(0, length);
    }

    public void changeLayoutFromDivide(View view) {
        if(view.getId() == R.id.divide_btn_back) {
            mediaPlayer.stop();
            onBackPressed();
        }
        else if(view.getId() == R.id.divide_btn_destroy) {
            mediaPlayer.stop();
            Intent intentBackHome = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intentBackHome);
        }
    }
}
