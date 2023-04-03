package com.devapp.smartrecord.editmenu.insertion;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devapp.smartrecord.R;

public class InsertionActivity extends AppCompatActivity {
    private ImageButton btnBack;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_insertion);
        getSupportActionBar().hide();

        btnBack = findViewById(R.id.edt_insert_btn_back);
        btnBack.setOnClickListener(view -> onBackPressed());
    }
}
