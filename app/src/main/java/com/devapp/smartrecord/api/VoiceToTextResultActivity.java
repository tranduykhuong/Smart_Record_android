package com.devapp.smartrecord.api;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;

public class VoiceToTextResultActivity extends AppCompatActivity {
    private final String API_OPENAI_KEY = "c2stY3ZSekE4VGhkODNVM3c2ZERlUE5UM0JsYmtGSnRiaDZiMDRZVUZTbnRCaHBQRVBz";
    private String AUDIO_FILE_PATH;
    private ImageButton btnBack;
    private TextView twResult;
    private ImageView btnPlay;

    private TextView btnChoiceLanguage;
    private TextView btnSummary;
    private TextView twEnglish;
    private TextView twDuration;
    private TextView twCurrentTime;
    private String jsonString;
    private int responseCode;
    private Handler handler;
    private String isSummary;
    private String decodedAPI;
    private String data;
    private int duration;
    private MediaPlayer mediaPlayer;
    private Runnable highlight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_to_text_result);
        getSupportActionBar().hide();
        handler = new Handler();
        mediaPlayer = new MediaPlayer();

        twCurrentTime = findViewById(R.id.tw_current_time);

        btnBack = findViewById(R.id.voice_to_text_result_btn_back);
        btnBack.setOnClickListener(view -> this.onBackPressed());

        btnChoiceLanguage = findViewById(R.id.btn_choice_language);
        btnChoiceLanguage.setOnClickListener(view -> showBottomSheetDialogLanguage());

        btnSummary = findViewById(R.id.btn_summary_text);
        btnSummary.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();

            String API_ENDPOINT = "https://api.openai.com/v1/completions";
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] decodedBytes = Base64.getDecoder().decode(API_OPENAI_KEY);
                    decodedAPI = new String(decodedBytes, StandardCharsets.UTF_8);

                    jsonString = "{\"id\":\"cmpl-70k5h5nlMOqU2ebyWtb5CHwLtrahb\",\"object\":\"text_completion\",\"created\":1680411529,\"model\":\"text-davinci-003\",\"choices\":[{\"text\":\"Loading...\",\"index\":0,\"logprobs\":null,\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":42,\"completion_tokens\":117,\"total_tokens\":159}}";
                    try {
                        URL url = new URL(API_ENDPOINT);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setRequestProperty("Authorization", "Bearer " + decodedAPI);
                        con.setDoOutput(true);

                        String jsonInputString = "{"
                                + "\"model\": \"text-davinci-003\","
                                + "\"prompt\": \"Rút gọn đoạn văn bản sau: " + data + "\","
                                + "\"temperature\": 0.7,"
                                + "\"max_tokens\": 256,"
                                + "\"top_p\": 1,"
                                + "\"frequency_penalty\": 0,"
                                + "\"presence_penalty\": 0"
                                + "}";

                        try (OutputStream os = con.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }
                        responseCode = con.getResponseCode();

                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        jsonString = response.toString();
                        br.close();
                        progressDialog.dismiss();

                        handler.post(foreGround);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (ProtocolException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "BackGroundThread");
            thread.start();
        });

        Intent intent = getIntent();
        data = intent.getStringExtra("data");
        isSummary = intent.getStringExtra("isSummary");
        AUDIO_FILE_PATH = intent.getStringExtra("PATH_KEY");
        twResult = findViewById(R.id.tw_data_result);
        twResult.setText(data.trim());
        Log.e(TAG, "onCreate: "+AUDIO_FILE_PATH);

        if (isSummary.equals("yes")) {
            btnSummary.didTouchFocusSelect();
            btnSummary.setText("");
        }

        // Khởi tạo MediaPlayer
        try {
            mediaPlayer.setDataSource(AUDIO_FILE_PATH);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
            btnPlay.setImageResource(R.drawable.ic_play);
            btnPlay.setTag("ic_play");
        });
        duration = mediaPlayer.getDuration();
        twDuration = findViewById(R.id.tw_duration);
        twDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", (duration / 1000) / 60, (duration / 1000) % 60));

        btnPlay = findViewById(R.id.btn_play_text);
        btnPlay.setOnClickListener(v -> {
            ImageView imageView = (ImageView) v;
            if (imageView.getTag() == null || imageView.getTag().equals("ic_play")) {
                imageView.setImageResource(R.drawable.ic_pause);
                imageView.setTag("ic_pause");

                mediaPlayer.start();
            } else {
                imageView.setImageResource(R.drawable.ic_play);
                imageView.setTag("ic_play");

                mediaPlayer.pause();
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

                            twCurrentTime.setText(String.format(Locale.getDefault(), "%02d:%02d", (currentPosition / 1000) / 60, (currentPosition / 1000) % 60));
                            handler.postDelayed(this, 1000); // update every 1 second
                        }
                    }
                };
                handler.post(highlight);
            }
        });
    }

    private Runnable foreGround = new Runnable() {
        @Override
        public void run() {
            try {
                try {
                    System.out.println("Response Code: " + responseCode);
//                    responseCode = 200;
                    if (responseCode == 200) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        Log.e(TAG, "run: " + jsonObject);
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");

                        Intent intent = new Intent(VoiceToTextResultActivity.this, VoiceToTextResultActivity.class);
                        intent.putExtra("data", jsonArray.getJSONObject(0).getString("text"));
                        intent.putExtra("isSummary", "yes");
                        intent.putExtra("PATH_KEY", AUDIO_FILE_PATH);
                        startActivity(intent);

//                        Log.e(TAG, "onCreate: " + jsonArray.getJSONObject(0).getString("utterance"));
                    } else {
                        Toast.makeText(VoiceToTextResultActivity.this, getString(R.string.error_convert_voice_to_text), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showBottomSheetDialogLanguage() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_language);

        twEnglish = bottomSheetDialog.findViewById(R.id.tw_english_setting);
        twEnglish.setText(getString(R.string.english) + " (" + getString(R.string.api_free) + ")");

        bottomSheetDialog.findViewById(R.id.english_setting).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.findViewById(R.id.vietnamese_setting).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (highlight != null) {
            handler.removeCallbacks(highlight);
        }
    }
}
