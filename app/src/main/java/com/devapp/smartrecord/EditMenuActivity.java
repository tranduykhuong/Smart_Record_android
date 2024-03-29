package com.devapp.smartrecord;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.editmenu.adjust.AdjustActivity;
import com.devapp.smartrecord.editmenu.combine.CombineActivity;
import com.devapp.smartrecord.editmenu.cut.CutActivity;
import com.devapp.smartrecord.editmenu.divide.DivideActivity;
import com.devapp.smartrecord.editmenu.harmonic.HarmonicActivity;
import com.devapp.smartrecord.editmenu.insertion.InsertionActivity;

public class EditMenuActivity extends AppCompatActivity {

    private String pathSound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_screen);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        pathSound = intent.getStringExtra("PATH_KEY");
    }

    public void changeLayoutFromAdjust(View view){
        switch (view.getId()) {
            case R.id.adjust_btn_back: {
                onBackPressed();
                break;
            }
            case R.id.adjust_btn_combine:{
                Intent intent = new Intent(this, CombineActivity.class);
                intent.putExtra("PATH_KEY", pathSound);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_insertion:{
                Intent intent = new Intent(this, InsertionActivity.class);
                intent.putExtra("PATH_KEY", pathSound);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_adjust: {
                Intent intent = new Intent(this, AdjustActivity.class);
                intent.putExtra("PATH_KEY", pathSound);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_divide:{
                Intent intent = new Intent(this, DivideActivity.class);
                intent.putExtra("PATH_KEY", pathSound);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_harmonic:{
                Intent intent = new Intent(this, HarmonicActivity.class);
                intent.putExtra("PATH_KEY", pathSound);
                startActivity(intent);
                break;
            }
            case R.id.adjust_btn_cut:{
                Intent intent = new Intent(this, CutActivity.class);
                intent.putExtra("PATH_KEY", pathSound);
                startActivity(intent);
                break;
            }
        }
    }
}
