package com.devapp.smartrecord.api;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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

public class VoiceToTextResultActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView twResult;

    private TextView btnChoiceLanguage;
    private TextView btnSummary;
    private TextView twEnglish;
    private String jsonString;
    private int responseCode;
    private Handler handler;
    private String isSummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_to_text_result);
        getSupportActionBar().hide();
        handler = new Handler();

        btnBack = findViewById(R.id.voice_to_text_result_btn_back);
        btnBack.setOnClickListener(view -> this.onBackPressed());

        btnChoiceLanguage = findViewById(R.id.btn_choice_language);
        btnChoiceLanguage.setOnClickListener(view -> showBottomSheetDialogLanguage());

        btnSummary = findViewById(R.id.btn_summary_text);
        btnSummary.setOnClickListener(view -> {
            String API_ENDPOINT = "https://api.openai.com/v1/completions";
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String apiKey = "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";
                    jsonString = "{\"id\":\"cmpl-70k5h5nlMOqU2ebyWtb5CHwLtrahb\",\"object\":\"text_completion\",\"created\":1680411529,\"model\":\"text-davinci-003\",\"choices\":[{\"text\":\" Bài test này sẽ giúp chúng ta biết được mức độ hiểu biết của chúng ta về chủ đề.\\n\\nTest để biết mức hiểu biết về chủ đề.\",\"index\":0,\"logprobs\":null,\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":42,\"completion_tokens\":117,\"total_tokens\":159}}";
                    handler.post(foreGround);
//                    try {
//                        URL url = new URL(API_ENDPOINT);
//                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                        con.setRequestMethod("POST");
//                        con.setRequestProperty("Content-Type", "application/json");
//                        con.setRequestProperty("Authorization", "Bearer " + apiKey);
//                        con.setDoOutput(true);
//
//                        String jsonInputString = "{"
//                                + "\"model\": \"text-davinci-003\","
//                                + "\"prompt\": \"Rút gọn đoạn văn sau:\\nAlo. Đây là một bài test.\","
//                                + "\"temperature\": 0.7,"
//                                + "\"max_tokens\": 256,"
//                                + "\"top_p\": 1,"
//                                + "\"frequency_penalty\": 0,"
//                                + "\"presence_penalty\": 0"
//                                + "}";
//
//                        try (OutputStream os = con.getOutputStream()) {
//                            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
//                            os.write(input, 0, input.length);
//                        }
//                        int responseCode = con.getResponseCode();
//
//                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
//                        StringBuilder response = new StringBuilder();
//                        String responseLine;
//                        while ((responseLine = br.readLine()) != null) {
//                            response.append(responseLine.trim());
//                        }
//                        jsonString = response.toString();
//                        br.close();
//                        handler.post(foreGround);
//                    } catch (MalformedURLException e) {
//                        throw new RuntimeException(e);
//                    } catch (ProtocolException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
                }
            }, "BackGroundThread");
            thread.start();
        });

        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        isSummary = intent.getStringExtra("isSummary");
        twResult = findViewById(R.id.tw_data_result);
        twResult.setText(data);
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
                        Log.e(TAG, "run: " + jsonObject);
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");

                        Intent intent = new Intent(VoiceToTextResultActivity.this, VoiceToTextResultActivity.class);
                        intent.putExtra("data", jsonArray.getJSONObject(0).getString("text"));
                        intent.putExtra("isSummary", "yes");
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
}
