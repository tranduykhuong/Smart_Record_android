package com.devapp.smartrecord.api;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VoiceToTextActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView btnConvert;
    private TextView btnChoiceLanguage;
    private TextView twEnglish;
    private String jsonString;
    private int responseCode;
    private Handler handler = new Handler();
    private static final String API_ENDPOINT = "https://api.fpt.ai/hmi/asr/general";

    private static final String API_KEY = "bgLggHqMJye7VQ5wFzsjcuBgzOf3BmTq";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_to_text);
        getSupportActionBar().hide();

        btnBack = findViewById(R.id.voice_to_text_btn_back);
        btnBack.setOnClickListener(view -> this.onBackPressed());

        btnChoiceLanguage = findViewById(R.id.btn_choice_language);
        btnChoiceLanguage.setOnClickListener(view -> showBottomSheetDialogLanguage());

        btnConvert = findViewById(R.id.btn_convert_voice_to_text);
        btnConvert.setOnClickListener(view -> {
            jsonString = "{\"status\": 0, \"id\": \"tranduykhuongit@gmail.com SmartRecord_22d1aaee-d777-4f08-9cfd-9d6f04a9d18c\", \"hypotheses\": [{\"utterance\": \"ALO \\u0111\\u00e2y l\\u00e0 1 b\\u00e0i . ALO .\", \"confidence\": 18.514267374889283}]}";
            try {
                Thread.sleep(1000);
                handler.post(foreGround);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            String AUDIO_FILE_PATH = "/storage/emulated/0/Recordings/Voice Recorder/Test.m4a";
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        URL url = new URL(API_ENDPOINT);
//                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                        conn.setRequestMethod("POST");
//                        conn.setRequestProperty("api_key", API_KEY);
//                        conn.setRequestProperty("language", "vi-VN");
//
//                        conn.setDoOutput(true);
//
//                        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//                        byte[] data = Files.readAllBytes(Paths.get(AUDIO_FILE_PATH));
//                        Log.e(TAG, "run: " + data);
//                        wr.write(data);
//                        wr.flush();
//                        wr.close();
//
//                        responseCode = conn.getResponseCode();
//
//                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
//
//                        String inputLine;
//                        StringBuilder response = new StringBuilder();
//                        while ((inputLine = in.readLine()) != null) {
//                            response.append(inputLine);
//                        }
//                        jsonString = response.toString();
//                        in.close();
//                        Log.e(TAG, "ResponseBg: " + jsonString);
//                        handler.post(foreGround);
//                    } catch (ProtocolException e) {
//                        throw new RuntimeException(e);
//                    } catch (MalformedURLException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }, "BackGroundThread");
//            thread.start();
        });
    }

    private Runnable foreGround = new Runnable() {
        @Override
        public void run() {
            try {
                try {
                    System.out.println("Response Code: " + responseCode);
                    responseCode = 200;
                    if (responseCode == 200) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONArray jsonArray = jsonObject.getJSONArray("hypotheses");

                        Intent intent = new Intent(VoiceToTextActivity.this, VoiceToTextResultActivity.class);
                        intent.putExtra("data", jsonArray.getJSONObject(0).getString("utterance"));
                        intent.putExtra("isSummary", "no");
                        startActivity(intent);
                        Log.e(TAG, "onCreate: " + jsonArray.getJSONObject(0).getString("utterance"));
                    } else {
                        Toast.makeText(VoiceToTextActivity.this, getString(R.string.error_convert_voice_to_text), Toast.LENGTH_SHORT).show();
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
}
