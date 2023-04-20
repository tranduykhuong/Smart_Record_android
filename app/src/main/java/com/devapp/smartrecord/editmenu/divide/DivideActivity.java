package com.devapp.smartrecord.editmenu.divide;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DivideActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int percentMin = 0;
    private int percentMax = 100;
    private int cntBtn = 0;
    private TextView currTime;
    private int duration;
    private TextView timeMax;
    private boolean isPlay = false;
    private byte[] waveform;
    ArrayList<Float> wf;
    private HorizontalScrollView hrzScrollView;
    private List<Entry> entries;
    private int currentPosition = 0;
    private final Handler handler = new Handler();
    private String fileName, pathSound;
    private TextView divideNameAudio;
    private ImageButton divideBtnPlay, divideBtn;
    private Button divideBtnConfirm;
    private float rate1, rate2, rate3;
    private File inputFile, outputFile1, outputFile2;
    private String divideTime, randomString;
    private int flagCnt = 0;
    String inputFilePath, outputFilePath;
    private int progressWidth;
    private LineChart chart;
    private int realWidth;

    // Một ArrayList để lưu trữ các giá trị thời gian
    ArrayList<String> timeList = new ArrayList<>();

    private Context context;
    private Runnable highlight;
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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        progressWidth = displayMetrics.widthPixels;
        realWidth = displayMetrics.widthPixels;

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

        entries = new ArrayList<>();
        hrzScrollView = findViewById(R.id.insert_horizontal);

        chart = findViewById(R.id.insert_chart);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(true);
        chart.getDescription().setEnabled(false);
        chart.setVisibleXRangeMinimum(60 * 1000f);
        // Vẽ lưới nền và thiết lập màu nền cho lưới
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);

        // Thiết lập khoảng cách giữa các đường lưới trên trục X và Y
        chart.getXAxis().setGranularity(1000f);
        chart.getXAxis().setSpaceMin(1000f);
        chart.getXAxis().setDrawLabels(false); //
        chart.getXAxis().setEnabled(false); //
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);

        File file1 = new File(pathSound);
        byte[] data = new byte[(int) file1.length()];
        try (FileInputStream inputStream = new FileInputStream(file1)) {
            inputStream.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = buffer.asIntBuffer();

        wf = new ArrayList<>();
        while (intBuffer.hasRemaining()) {
            int sample = intBuffer.get();
            float amplitude = (float) (Math.abs((float) sample) / 32768.0);
            wf.add(amplitude);
        }

        double[] samples = new double[data.length / 2];
        for (int i = 0; i < samples.length; i++) {
            short sample = (short) (((data[i * 2 + 1] & 0xff) << 8) | (data[i * 2] & 0xff));
            samples[i] = (double) sample / Short.MAX_VALUE;
        }

        // Chuyển sang waveform
        waveform = new byte[samples.length + 1];
        for (int i = 0; i < samples.length; i = i + 1) {
            waveform[i] = (byte) (samples[i] * 100);
        }

        updateChart();

        // Đường dẫn tệp MP3 đầu vào
        inputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName;
        outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/";
        randomString = generateRandomString(3);
        inputFile = new File(inputFilePath);
        outputFile1 = new File(outputFilePath, "divide1 " + FilenameUtils.removeExtension(fileName) + "_" + randomString + ".mp3");
        outputFile2 = new File(outputFilePath, "divide2 " + FilenameUtils.removeExtension(fileName) + "_" + randomString + ".mp3");

        divideNameAudio.setText(fileName);
        //Mở file chuẩn bị
        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/Recordings/" + fileName);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
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
                highlight = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int currentPosition = mediaPlayer.getCurrentPosition();
                            currTime.setText(formatTime(currentPosition));
                            // do something with currentPosition
                            chart.highlightValue((int) (currentPosition * (waveform.length * 1f / (duration - 2.2))), 0);

                            rate1 = 1.0f * realWidth / chart.getWidth();
                            rate2 = 1.0f * currentPosition / duration;

                            if (rate3 > 0.01 && rate2 > rate3) {
                                rate3 = rate3 + rate1;
                                hrzScrollView.smoothScrollTo(progressWidth, 0);
                                progressWidth = progressWidth + realWidth;
                            }
                            handler.postDelayed(this, 10);
                            if (currentPosition > percentMax / 100f * duration) {
                                mediaPlayer.pause();
                                mediaPlayer.seekTo((int) (percentMin / 100f * duration));

                                divideBtnPlay.setImageResource(R.drawable.ic_play_combine_main);
                            }
                        }
                    }
                };
                handler.post(highlight);
            }
        });

        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            currTime.setText("00:00:00");
            isPlay = false;
            cntBtn = 0;
            divideBtnPlay.setImageResource(R.drawable.ic_play_combine_main);
        });
        divideBtn.setOnClickListener(view -> {
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
                        Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.announce_divide_successfully), Toast.LENGTH_SHORT).show();
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

    private void updateChart() {
        int curMinWave = (int) (waveform.length * (percentMin / 100f));
        int curMaxWave = (int) (waveform.length * (percentMax / 100f));
        Log.e(TAG, "updateChart: " + curMinWave + " " + curMaxWave);

        entries.clear();
        for (int i = curMinWave; i < curMaxWave; i = i + 134) {
            if (Math.abs(waveform[i]) < 60)
                entries.add(new Entry(i, waveform[i] / 10));
            else if (Math.abs(waveform[i]) < 80)
                entries.add(new Entry(i, waveform[i] / 6));
            else if (Math.abs(waveform[i]) < 90)
                entries.add(new Entry(i, waveform[i] / 4));
            else if (Math.abs(waveform[i]) < 116)
                entries.add(new Entry(i, waveform[i] / 3));
            else
                entries.add(new Entry(i, waveform[i] / 2));
        }
        Log.e(TAG, "entries: " + entries.size());

        int range = Math.min(entries.size() * 2, 9000);
        chart.setMinimumWidth(range);

        rate3 = 1.0f * progressWidth / range;

        LineDataSet dataSet = new LineDataSet(entries, "lb1");

        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.BLACK);
        dataSet.setLineWidth(1f);
        dataSet.setHighlightLineWidth(2f);
        dataSet.setDrawHorizontalHighlightIndicator(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

        chart.highlightValue(0, 0);
    }

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
