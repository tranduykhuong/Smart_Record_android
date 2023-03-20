package com.devapp.smartrecord.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rcvHomeAudio;
    private  HomeAudioAdapter homeAudioAdapter;
    private List<Audio> audioList;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final LinearLayout linearLayoutSortType = binding.homeWrapSortType;
        linearLayoutSortType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.home_sort_type_item, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                TextView homeDateSort = binding.homeDateSort;
                TextView sortTitle = popupView.findViewById(R.id.home_sort_item_title);
                TextView sortByName = popupView.findViewById(R.id.home_sort_item_by_name);
                TextView sortByDate = popupView.findViewById(R.id.home_sort_item_by_date);
                TextView sortBySize = popupView.findViewById(R.id.home_sort_item_by_size);
                TextView cancelSort = popupView.findViewById(R.id.home_sort_item_by_destroy);

                sortTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.update();
                    }
                });

                sortByDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortByDate.getText());
                        popupWindow.dismiss();
                    }
                });

                sortByName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortByName.getText());
                        popupWindow.dismiss();
                    }
                });

                sortBySize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortBySize.getText());
                        popupWindow.dismiss();
                    }
                });

                cancelSort.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

        rcvHomeAudio = binding.homeRcvAudioList;
        rcvHomeAudio.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList = new ArrayList<>();
        homeAudioAdapter = new HomeAudioAdapter(getContext());
        homeAudioAdapter.setData(getAudioList());
        rcvHomeAudio.setAdapter(homeAudioAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<Audio> getAudioList() {
        for (int i = 0; i < 10; i++) {
            audioList.add(new Audio("Đại học Khoa học tự nhiên", "2:45", "100", "26/02/2023 10:11", R.drawable.ic_play_audio_item));
        }
        return audioList;
    }

}