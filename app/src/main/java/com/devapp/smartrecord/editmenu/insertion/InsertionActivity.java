package com.devapp.smartrecord.editmenu.insertion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;
import com.devapp.smartrecord.ui.home.HomeAudioAdapter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InsertionActivity extends AppCompatActivity {
    private ImageButton btnBack, btnInsert;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_insertion);
        getSupportActionBar().hide();

        btnBack = findViewById(R.id.edt_insert_btn_back);
        btnBack.setOnClickListener(view -> onBackPressed());

        btnInsert = findViewById(R.id.insert_btn);

         btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InsertionActivity.this, InsertionListFile.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "hihi", Toast.LENGTH_SHORT).show();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { // kiểm tra requestCode đã gửi từ activity cũ
            if (resultCode == Activity.RESULT_OK && requestCode == 1) {
                String result = data.getStringExtra("result"); // lấy dữ liệu trả về từ adapter mới
                // làm gì đó với dữ liệu trả về
            }
        }
    }
}
