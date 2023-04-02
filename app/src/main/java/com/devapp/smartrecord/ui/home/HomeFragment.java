package com.devapp.smartrecord.ui.home;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentHomeBinding;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements HomeAudioAdapter.OnItemClickListener{

    private static final int SORT_BY_NAME = 0;
    private static final int SORT_BY_DATE = 1;
    private static final int SORT_BY_SIZE = 2;
    private int currentSortType = SORT_BY_DATE; // mặc định sắp xếp theo ngày
    private RecyclerView rcvHomeAudio;
    private  HomeAudioAdapter homeAudioAdapter;
    private List<Audio> audioList;
    private TextView totalAmountAudio;
    private int size;
    private File[] files;
    private File recordingsDirectory;
    private SearchView searchViewAudio;
    private double sumCapacity = 0;
    private TextView totalCapacityAudio;
    private TextView capacityUnit;
    private FragmentHomeBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        final LinearLayout linearLayoutSortType = binding.homeWrapSortType;
        totalAmountAudio = binding.homeAmountAudio;
        totalCapacityAudio = binding.homeCapacityAudio;
        capacityUnit = binding.capacityAudioUnit;

        searchViewAudio = binding.searchViewAudioHome;
        searchViewAudio.clearFocus();
        searchViewAudio.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
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

                        currentSortType = SORT_BY_DATE;
                        //sort the list by date in descending order
                        Collections.sort(audioList, new Comparator<Audio>() {
                            @Override
                            public int compare(Audio audio1, Audio audio2) {
                                Date date1 = null;
                                Date date2 = null;
                                try {
                                    date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(audio1.getCreateDate());
                                    date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(audio2.getCreateDate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (date1 != null && date2 != null) {
                                    return date2.compareTo(date1);
                                } else {
                                    return 0;
                                }
                            }
                        });

                        // cập nhật lại adapter và hiển thị danh sách mới
                        homeAudioAdapter.setData(audioList);
                        homeAudioAdapter.notifyDataSetChanged();
                    }
                });

                sortByName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortByName.getText());
                        popupWindow.dismiss();

                        currentSortType = SORT_BY_NAME;

                        // sắp xếp danh sách theo tên
                        Collections.sort(audioList, new Comparator<Audio>() {
                            @Override
                            public int compare(Audio audio1, Audio audio2) {
                                return audio1.getName().compareTo(audio2.getName());
                            }
                        });

                        // cập nhật lại adapter và hiển thị danh sách mới
                        homeAudioAdapter.setData(audioList);
                        homeAudioAdapter.notifyDataSetChanged();
                    }
                });

                sortBySize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeDateSort.setText(sortBySize.getText());
                        popupWindow.dismiss();

                        currentSortType = SORT_BY_SIZE;
                        NumberFormat format = NumberFormat.getInstance(Locale.US);
                        Collections.sort(audioList, new Comparator<Audio>() {
                            @Override
                            public int compare(Audio audio1, Audio audio2) {
                                String size1Str = audio1.getSize();
                                String size2Str = audio2.getSize();
                                // Kiểm tra nếu số đầu tiên của chuỗi có dấu ',' thì thay thế bằng '.'
                                if (size1Str.indexOf(',') == 1) {
                                    size1Str = size1Str.replace(',', '.');
                                }
                                if (size2Str.indexOf(',') == 1) {
                                    size2Str = size2Str.replace(',', '.');
                                }
                                try {
                                    Float size1 = format.parse(size1Str).floatValue();
                                    Float size2 = format.parse(size2Str).floatValue();
                                    return Float.compare(size1, size2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            }
                        });



                        // cập nhật lại adapter và hiển thị danh sách mới
                        homeAudioAdapter.setData(audioList);
                        homeAudioAdapter.notifyDataSetChanged();
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
        homeAudioAdapter = new HomeAudioAdapter(getContext(), this);

        homeAudioAdapter.setData(getAudioList());
        rcvHomeAudio.setAdapter(homeAudioAdapter);

        totalAmountAudio.setText(String.valueOf(recordingsDirectory.listFiles().length));
        if(sumCapacity >= 1024) {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity));
        }

        return root;
    }

    private void filterList(String text) {
        List<Audio> filterList = new ArrayList<>();
        for (Audio itemAudio : audioList) {
            if (itemAudio.getName().toLowerCase().contains(text.toLowerCase())){
                filterList.add(itemAudio);
            }
        }
        if (filterList.isEmpty()) {
            homeAudioAdapter.setData(null);
            Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        }else {
            homeAudioAdapter.setData(filterList);
        }
    }

    @Override
    public void onItemClick(int position) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        totalAmountAudio.setText(String.valueOf(recordingsDirectory.listFiles().length));
        if(sumCapacity >= 1024) {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity));
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @NonNull
    private List<Audio> getAudioList() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        audioList = new ArrayList<>();
        recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/Recorder/");
        size = recordingsDirectory.listFiles().length;
        if (recordingsDirectory.exists()) {
            files = recordingsDirectory.listFiles();
            if (files != null) {
                double tempCapacity = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") || file.getName().endsWith(".aac")) {

                        String fileName = file.getName();
                        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
                        tempCapacity += (1.0 * file.length() / (1024 * 1.0));
                        Date lastModifiedDate = new Date(file.lastModified());
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        audioList.add(new Audio(fileName, getFileDuration(file), String.valueOf(fileSize), formattedDate, R.drawable.ic_play_audio_item));
                    }
                }
                if(sumCapacity == 0) {
                    sumCapacity += tempCapacity;
                } else {
                    sumCapacity = tempCapacity;
                }

            }
            Collections.sort(audioList, new Comparator<Audio>() {
                @Override
                public int compare(Audio audio1, Audio audio2) {
                    Date date1 = null;
                    Date date2 = null;
                    try {
                        date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(audio1.getCreateDate());
                        date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(audio2.getCreateDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    } else {
                        return 0;
                    }
                }
            });
        }
        return audioList;
    }

    @NonNull
    private String getFileDuration(File file) {
        long durationInMillis = 0;

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            durationInMillis = Long.parseLong(durationStr);
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}