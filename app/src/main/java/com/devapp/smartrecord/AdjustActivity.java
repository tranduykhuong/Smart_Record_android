package com.devapp.smartrecord;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.editmenu.combine.CombineActivity;
import com.devapp.smartrecord.editmenu.divide.DivideActivity;
import com.devapp.smartrecord.editmenu.harmonic.HarmonicActivity;
import com.devapp.smartrecord.editmenu.insertion.InsertionActivity;

public class AdjustActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_screen);
        getSupportActionBar().hide();

    }

    public void changeLayoutFromAdjust(View view){
        switch (view.getId()) {
            case R.id.adjust_btn_back: {
                Intent intent = new Intent(this, ReplayActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_combine:{
                Intent intent = new Intent(this, CombineActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_insertion:{
                Intent intent = new Intent(this, InsertionActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_adjust:{
                Intent intent = new Intent(this, com.devapp.smartrecord.editmenu.adjust.AdjustActivity.class);
            case R.id.adjust_btn_divide:{
                Intent intent = new Intent(this, DivideActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_harmonic:{
                Intent intent = new Intent(this, HarmonicActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
