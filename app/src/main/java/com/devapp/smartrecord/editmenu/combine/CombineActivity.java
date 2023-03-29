package com.devapp.smartrecord.editmenu.combine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.AdjustActivity;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ui.home.Audio;

import java.util.ArrayList;
import java.util.List;

public class CombineActivity extends AppCompatActivity {

    private RecyclerView rcvCombineAudio;
    private CombineAudioAdapter combineAudioAdapter;
    private List<Audio> audioList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine);
        getSupportActionBar().hide();

        rcvCombineAudio = (RecyclerView) findViewById(R.id.combine_rcv_audio_list);
        rcvCombineAudio.setLayoutManager(new LinearLayoutManager(this));
        audioList = new ArrayList<>();
        combineAudioAdapter = new CombineAudioAdapter(this);
        combineAudioAdapter.setData(getAudioList());
        rcvCombineAudio.setAdapter(combineAudioAdapter);
    }

    public void changeLayoutFromCombine(View view) {
        switch (view.getId()) {
            case R.id.combine_btn_back: {
                Intent intent = new Intent(this, AdjustActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    private List<Audio> getAudioList() {
        for (int i = 0; i < 2; i++) {
            audioList.add(new Audio("Đại học Khoa học tự nhiên", "2:45", "100", "26/02/2023 10:11", R.drawable.ic_play_audio_item));
        }
        return audioList;
    }
}
