package com.devapp.smartrecord.editmenu.harmonic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.AdjustActivity;
import com.devapp.smartrecord.R;

public class HarmonicActivity extends AppCompatActivity {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harmonic);
        getSupportActionBar().hide();
    }

    public void changeLayoutFromHarmonic(View view) {
        switch (view.getId()) {
            case R.id.harmonic_btn_back: {
                Intent intent = new Intent(this, AdjustActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
