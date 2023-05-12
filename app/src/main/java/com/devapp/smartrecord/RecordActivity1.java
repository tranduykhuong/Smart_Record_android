package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.suman.voice.graphviewlibrary.GraphView;
import com.suman.voice.graphviewlibrary.WaveSample;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecordActivity1 extends AppCompatActivity {
    private boolean flagRecording;
    private ImageButton btnPlay;
    private TextView txtRecordName;
    private TextView twCurrentTime;
    private GraphView graphView;
    private TelephonyManager telephonyManager;
    private JSONArray jsonArray;
    private final List<WaveSample> pointList = new ArrayList<>();
    private String timeWhenNote, fileName;
    private int count = 0;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Objects.requireNonNull(getSupportActionBar()).hide();
        jsonArray = new JSONArray();
        pointList.clear();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter("record-activity-event"));

        twCurrentTime = findViewById(R.id.record_current_time);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        flagRecording = sharedPreferences.getBoolean("isRecording", false);

        ImageButton btnStop = findViewById(R.id.record_btn_stop);
        ImageButton btnNote = findViewById(R.id.record_btn_note);
        btnPlay = findViewById(R.id.record_btn_play);
        txtRecordName = findViewById(R.id.record_name);
        graphView = findViewById(R.id.graphView);
        graphView.setWaveLengthPX(13);

        ConfigurationClass config = new ConfigurationClass(this);
        config.getConfig();
        if (config.getThemeMode() == 1) {
            graphView.setGraphColor(Color.WHITE);
            graphView.setCanvasColor(Color.BLACK);
            graphView.setTimeColor(Color.WHITE);
        } else {
            graphView.setGraphColor(Color.BLACK);
            graphView.setCanvasColor(Color.WHITE);
            graphView.setTimeColor(Color.BLACK);
        }

        if (graphView != null) {
            graphView.setMasterList(pointList);
            graphView.startPlotting();
        }

        startRecord();

        btnPlay.setOnClickListener(view -> recordAudio());

        btnStop.setOnClickListener(view -> {
            flagRecording = false;
            btnPlay.setImageResource(R.drawable.ic_play_record);
            confirmDelete();
        });

        if (flagRecording) {
            ResumeRecord();
        }

        btnNote.setOnClickListener(view -> {

            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.record_modal_note, null);

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT ;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window token
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

            TextView headingTimeNote = popupView.findViewById(R.id.record_time_note);
            TextView btnDestroyNote = popupView.findViewById(R.id.record_modal_destroy_note);
            TextView btnOkRecordNote = popupView.findViewById(R.id.record_modal_ok_note);
            EditText edtNote = popupView.findViewById(R.id.record_note_edt);

            timeWhenNote = (String) twCurrentTime.getText();

            headingTimeNote.setText(timeWhenNote);

            btnDestroyNote.setOnClickListener(v -> popupWindow.dismiss());

            btnOkRecordNote.setOnClickListener(view1 -> {
                jsonArray.put(timeWhenNote + " - " + edtNote.getText());

                String json = jsonArray.toString();

                SharedPreferences sharedPreferences1 = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putString((String) txtRecordName.getText(), json);
                editor.apply();

                Toast.makeText(this, view.getContext().getString(R.string.add_successfull), Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            popupWindow.setOnDismissListener(() -> {
             flagRecording = true;
            });
        });
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current_time = intent.getIntExtra("CURRENT_TIME", 0);
            fileName = intent.getStringExtra("FILE_NAME");
            int x = intent.getIntExtra("GRAPH_COOR", 0);
            int y = intent.getIntExtra("GRAPH_VALUE", 0);
            if (current_time != 0) {
                twCurrentTime.setText(String.format(Locale.getDefault(), "%02d:%02d", current_time / 60, current_time % 60));
                txtRecordName.setText(fileName);
            } else {
                pointList.add(new WaveSample(x, y));
            }
        }
    };

    private void confirmDelete(){
        PauseRecord();
        if (graphView!= null) {
            graphView.stopPlotting();
        }
        AlertDialog.Builder alertDiaglog = new AlertDialog.Builder(this);
        alertDiaglog.setTitle(this.getString(R.string.save_record_announce));
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage(this.getString(R.string.YN_save_announce));
        alertDiaglog.setPositiveButton(this.getString(R.string.save_announce), (dialogInterface, i) -> {
            Intent intent = new Intent("record-event");
            intent.putExtra("ACTION_RECORD", "SAVE_RECORDING");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            Intent returnHome = new Intent(this, HomeActivity.class);
            startActivity(returnHome);
        });
        alertDiaglog.setNegativeButton(this.getString(R.string.delete_announce), (dialogInterface, i) -> {
            Intent intent = new Intent("record-event");
            intent.putExtra("ACTION_RECORD", "NO_SAVE_RECORDING");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            Intent returnHome = new Intent(this, HomeActivity.class);
            startActivity(returnHome);
        });

        alertDiaglog.show();
    }

    public void recordAudio() {
        onRecord(!flagRecording);
        flagRecording = !flagRecording;
    }

    private void startRecord(){
        Toast.makeText(getApplicationContext(), this.getString(R.string.record_announce), Toast.LENGTH_SHORT).show();
        btnPlay.setImageResource(R.drawable.ic_pause_record);
        File folder = new File(Environment.getExternalStorageDirectory() + "/Recordings");

        if(!folder.exists())
        {
            folder.mkdir();
        }

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void onRecord(boolean start) {
        if (start)
        {
            ResumeRecord();
        }
        else
        {
            PauseRecord();
        }
    }

    public void ResumeRecord(){
        btnPlay.setImageResource(R.drawable.ic_pause_record);

        if (graphView != null) {
            graphView.startPlotting();
            graphView.resume();
        }

        Intent intent = new Intent("record-event");
        intent.putExtra("ACTION_RECORD", "RESUME_RECORDING");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void PauseRecord()
    {
        btnPlay.setImageResource(R.drawable.ic_play_record);

        if (graphView != null) {
            graphView.pause();
        }

        Intent intent = new Intent("record-event");
        intent.putExtra("ACTION_RECORD", "PAUSE_RECORDING");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                PauseRecord();
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    protected void onResume() {
        count++;
        super.onResume();
        if(count > 1)
        {
            if (graphView != null) {
                graphView.startPlotting();
                graphView.resume();
            }
        }
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        if (graphView!= null) {
            graphView.stopPlotting();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        if (graphView!= null) {
            graphView.stopPlotting();
        }
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    public void changeLayoutFromRecord(View view){
        if (graphView!= null) {
            graphView.stopPlotting();
        }
        if (view.getId() == R.id.record_btn_back) {
            finish();
        }
    }
}