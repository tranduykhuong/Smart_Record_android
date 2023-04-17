package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.devapp.smartrecord.services.RecordingActivity;
import com.suman.voice.graphviewlibrary.GraphView;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecordActivity extends AppCompatActivity {
    private boolean flagRecording = false;
    long timeWhenPaused = 0;
    private String fileName;
    private ImageButton btnPlay;
    private Chronometer chronometerTime;
    private TextView txtRecordName;
    private GraphView graphView;
    private RecordingActivity recorder;
    private TelephonyManager telephonyManager;
    private JSONArray jsonArray;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Objects.requireNonNull(getSupportActionBar()).hide();
        jsonArray = new JSONArray();

        ImageButton btnStop = findViewById(R.id.record_btn_stop);
        ImageButton btnNote = findViewById(R.id.record_btn_note);
        btnPlay = findViewById(R.id.record_btn_play);
        chronometerTime = findViewById(R.id.record_time_recording);
        txtRecordName = findViewById(R.id.record_name);
        graphView = findViewById(R.id.graphView);
        graphView.setGraphColor(Color.rgb(18, 17, 17));
        graphView.setCanvasColor(Color.rgb(255, 255, 255));
        graphView.setTimeColor(Color.rgb(0, 0, 0));
        graphView.setWaveLengthPX(13);

        recorder = RecordingActivity.getInstance();

        getLocation();

        ConfigurationClass config = new ConfigurationClass(getApplicationContext());
        config.getConfig();
        recorder.setFileExt("." + config.getFileFormat());

        startRecord();

        btnPlay.setOnClickListener(view -> recordAudio());

        btnStop.setOnClickListener(view -> {
            flagRecording = true;
            btnPlay.setImageResource(R.drawable.ic_play_record);
            chronometerTime.setBase(SystemClock.elapsedRealtime());
            chronometerTime.stop();
            confirmDelete();
        });

        btnNote.setOnClickListener(view -> {
            PauseRecord();

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

            long seconds = timeWhenPaused / 1000;
            String TimePause = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
            headingTimeNote.setText(TimePause);

            btnDestroyNote.setOnClickListener(v -> popupWindow.dismiss());

            btnOkRecordNote.setOnClickListener(view1 -> {
                jsonArray.put(TimePause + " - " + edtNote.getText());

                String json = jsonArray.toString();

                SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(recorder.getFileName(), json);
                editor.apply();

                Toast.makeText(this, view.getContext().getString(R.string.add_successfull), Toast.LENGTH_LONG).show();
                popupWindow.dismiss();
            });

            popupWindow.setOnDismissListener(() -> {
             ResumeRecord();
            });
        });

    }
    private void deleteRecord(){
        recorder.stopWithNoSave();
        Toast.makeText(getApplicationContext(), this.getString(R.string.remove_file), Toast.LENGTH_LONG).show();

        Intent returnHome = new Intent(this, HomeActivity.class);
        startActivity(returnHome);
    }
    private void saveRecord(){
        recorder.stopRecording();
        Toast.makeText(getApplicationContext(), this.getString(R.string.save_file) + recorder.getOutputFilePath(), Toast.LENGTH_LONG).show();

        Intent returnHome = new Intent(this, HomeActivity.class);
        startActivity(returnHome);
    }
    private void confirmDelete(){
        AlertDialog.Builder alertDiaglog = new AlertDialog.Builder(this);
        alertDiaglog.setTitle("Lưu bản ghi");
        alertDiaglog.setIcon(R.mipmap.ic_launcher);
        alertDiaglog.setMessage("Bạn có muốn lưu bản ghi?");
        alertDiaglog.setPositiveButton("Lưu", (dialogInterface, i) -> saveRecord());
        alertDiaglog.setNegativeButton("Xóa", (dialogInterface, i) -> deleteRecord());

        alertDiaglog.show();
    }

    @SuppressLint("SetTextI18n")
    public void getLocation(){
        //GET LOCATION
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        double latitude = 0, longitude = 0;

        if(location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                String finalAddress = addressString.substring(0, addressString.indexOf(','));
                finalAddress = finalAddress.replace("/", "-");
                txtRecordName.setText(finalAddress);
                recorder.setOutputFilePath(finalAddress);
            }
        } catch (IOException e) {
            txtRecordName.setText("Record");
            recorder.setOutputFilePath("Record");
            e.printStackTrace();
        }
    }

    public void recordAudio(){
        onRecord(flagRecording);
        flagRecording = !flagRecording;
    }

    private void startRecord(){
        Toast.makeText(getApplicationContext(), "Record...", Toast.LENGTH_SHORT).show();
        btnPlay.setImageResource(R.drawable.ic_pause_record);
        File folder = new File(Environment.getExternalStorageDirectory() + "/Recordings");

        if(!folder.exists())
        {
            folder.mkdir();
        }
        chronometerTime.setBase(SystemClock.elapsedRealtime());
        chronometerTime.start();

        recorder.startRecording();
        recorder.startPlotting(graphView);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void onRecord(boolean start) {
        if(start)
        {
            ResumeRecord();
        }
        else
        {
            PauseRecord();
        }
    }

    public void ResumeRecord(){
        Toast.makeText(this, "Resume", Toast.LENGTH_LONG).show();
        btnPlay.setImageResource(R.drawable.ic_pause_record);

        chronometerTime.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
        chronometerTime.start();
        recorder.resumeRecording();

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void PauseRecord()
    {
        Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show();
        btnPlay.setImageResource(R.drawable.ic_play_record);

        recorder.pauseRecording();

        List samples = recorder.getSamples();
        graphView.showFullGraph(samples);

        timeWhenPaused = SystemClock.elapsedRealtime() - chronometerTime.getBase();
        chronometerTime.stop();
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
        super.onResume();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        Toast.makeText(getApplicationContext(), "Đang dừng nè má", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(getApplicationContext(), "Đang hủy nè má", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    public void changeLayoutFromRecord(View view){
        if (view.getId() == R.id.record_btn_back) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isRecording", flagRecording);
            editor.apply();
            finish();
        }
    }
}