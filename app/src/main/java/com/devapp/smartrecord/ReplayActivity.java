package com.devapp.smartrecord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReplayActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replay_screen);
        getSupportActionBar().hide();
    }

    public void changeLayoutFromReplay(View view){
        switch (view.getId()) {
            case R.id.replay_btn_adjust: {
                Intent intent = new Intent(this, AdjustActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
