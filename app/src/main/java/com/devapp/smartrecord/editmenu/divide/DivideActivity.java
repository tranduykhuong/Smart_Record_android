package com.devapp.smartrecord.editmenu.divide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.EditMenuActivity;
import com.devapp.smartrecord.R;

public class DivideActivity extends AppCompatActivity {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_divide);
        getSupportActionBar().hide();
    }

    public void changeLayoutFromDivide(View view) {
        switch (view.getId()) {
            case R.id.divide_btn_back: {
                Intent intent = new Intent(this, EditMenuActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
