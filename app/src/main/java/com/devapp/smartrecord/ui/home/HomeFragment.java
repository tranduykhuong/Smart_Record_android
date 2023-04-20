package com.devapp.smartrecord.ui.home;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD
import android.widget.ImageButton;
import android.widget.ImageView;
=======
>>>>>>> a0bc3779a3f394dc3b1b0dd0c4a0a8381907bc78
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ReplayActivity;
import com.devapp.smartrecord.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.FileFilter;
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
    private TextView homeSelectAll;
    private int size;
    private File[] files;
    private File recordingsDirectory;
    private SearchView searchViewAudio;
    private double sumCapacity = 0;
    private TextView totalCapacityAudio;
    private TextView capacityUnit, homeTitle;
    private FragmentHomeBinding binding;
    private Context context;
<<<<<<< HEAD
    private boolean isEdit, isTotalChecked = false;
    private TextView totalAudio, totalSizeAudio, textAudio, textCatholic, textTotalChoice;
    private LinearLayout recordLayout;
    private ImageView imageTotalChoice;
    private RelativeLayout infoLayout;
    private boolean[] selectedItems;
    private HomeChildFragment audioChild;
    private int[] listItemChoice;
    private OnDataPass dataPasser;

    public interface OnDataPass {
        void onDataPassHome(boolean data);
    }

    // Method để gửi dữ liệu về Activity
    public void passData(boolean isEdit) {
        dataPasser.onDataPassHome(isEdit);
    }
=======
>>>>>>> a0bc3779a3f394dc3b1b0dd0c4a0a8381907bc78

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        final LinearLayout linearLayoutSortType = binding.homeWrapSortType;
        totalAmountAudio = binding.homeAmountAudio;
        totalCapacityAudio = binding.homeCapacityAudio;
        capacityUnit = binding.capacityAudioUnit;

<<<<<<< HEAD
        totalAudio = binding.homeAmountAudio;
        totalSizeAudio = binding.homeCapacityAudio;
        textAudio = binding.homeTitleAudio;
        textCatholic = binding.capacityAudioUnit;
        infoLayout = binding.homeWrapInfoAudio;
        recordLayout = binding.homeWrapRecordFeature;
        homeTitle = binding.homeTitleAudio;
        homeSelectAll = binding.homeSelectAll;

        rcvHomeAudio = binding.homeRcvAudioList;
        rcvHomeAudio.setLayoutManager(new LinearLayoutManager(getContext()));
        homeAudioAdapter = new HomeAudioAdapter(getActivity(), this);

        homeAudioAdapter.setData(getAudioList());
        rcvHomeAudio.setAdapter(homeAudioAdapter);

        selectedItems = new boolean[audioList.size()];
        Arrays.fill(selectedItems, false);


        // Lấy dữ liệu được truyền vào từ activity
        Bundle args = getArguments();
        if (args != null){
            isEdit = args.getBoolean("isEditHome");
        }

        showMultiFolder();

=======
>>>>>>> a0bc3779a3f394dc3b1b0dd0c4a0a8381907bc78
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
        linearLayoutSortType.setOnClickListener(view -> {
            // inflate the layout of the popup window
            LayoutInflater inflater1 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater1.inflate(R.layout.home_sort_type_item, null);

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

            sortTitle.setOnClickListener(view1 -> popupWindow.update());
            sortByDate.setOnClickListener(view12 -> {
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
            });
            sortByName.setOnClickListener(view13 -> {
                homeDateSort.setText(sortByName.getText());
                popupWindow.dismiss();

                currentSortType = SORT_BY_NAME;

                // sắp xếp danh sách theo tên
                Collections.sort(audioList, (audio1, audio2) -> audio1.getName().compareTo(audio2.getName()));

                // cập nhật lại adapter và hiển thị danh sách mới
                homeAudioAdapter.setData(audioList);
                homeAudioAdapter.notifyDataSetChanged();
            });
            sortBySize.setOnClickListener(view14 -> {
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
            });
            cancelSort.setOnClickListener(view15 -> popupWindow.dismiss());

        });

<<<<<<< HEAD
=======
        rcvHomeAudio = binding.homeRcvAudioList;
        rcvHomeAudio.setLayoutManager(new LinearLayoutManager(getContext()));
        homeAudioAdapter = new HomeAudioAdapter(getActivity(), this);

        homeAudioAdapter.setData(getAudioList());

        rcvHomeAudio.setAdapter(homeAudioAdapter);


>>>>>>> a0bc3779a3f394dc3b1b0dd0c4a0a8381907bc78
        if (recordingsDirectory != null && recordingsDirectory.listFiles() != null) {
            File[] files = recordingsDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile(); // Chỉ chấp nhận các tệp (file) và không chấp nhận thư mục (folder)
                }
            });

            if (files != null) {
<<<<<<< HEAD
                totalAudio.setText(String.valueOf(files.length));
=======
                totalAmountAudio.setText(String.valueOf(files.length));
>>>>>>> a0bc3779a3f394dc3b1b0dd0c4a0a8381907bc78
            }
        }
        if(sumCapacity >= 1024) {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityAudio.setText(decimalFormat.format(sumCapacity));
        }
        return root;
    }

<<<<<<< HEAD
    private void showMultiFolder(){
        if (isEdit) {
            totalSizeAudio.setVisibility(View.GONE);
            textCatholic.setVisibility(View.GONE);
            recordLayout.setVisibility(View.GONE);
            totalAudio.setVisibility(View.GONE);
            textAudio.setVisibility(View.GONE);

            imageTotalChoice = binding.homeTotalChoice;
            imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
            homeSelectAll.setText("Chọn tất cả");

//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoLayout.getLayoutParams();
            RelativeLayout.LayoutParams paramsText = (RelativeLayout.LayoutParams) totalAudio.getLayoutParams();
//            params.leftMargin = 30;
            paramsText.leftMargin = 14;

            imageTotalChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isTotalChecked) {
                        imageTotalChoice.setImageResource(R.drawable.ic_circle_checked_folder);
                        for (int i = 0; i < audioList.size(); i++) {
                            audioList.get(i).setImage(R.drawable.ic_circle_checked_folder);
                        }
                        homeAudioAdapter.notifyDataSetChanged();
                        Arrays.fill(selectedItems, true);
                        isTotalChecked = true;
                    } else {
                        imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
                        for (int i = 0; i < audioList.size(); i++) {
                            audioList.get(i).setImage(R.drawable.ic_circle_folder);
                        }
                        homeAudioAdapter.notifyDataSetChanged();
                        Arrays.fill(selectedItems, false);
                        isTotalChecked = false;
                    }
                }
            });

            choiceMultiFolder();
            showChildFragment();
        }
    }

    private void choiceMultiFolder(){
        if (audioList != null){
            for (int i = 0; i < audioList.size(); i++){
                audioList.get(i).setImage(R.drawable.ic_circle_folder);
            }
            homeAudioAdapter.notifyDataSetChanged();
        }
    }

    private void showChildFragment() {
        // Replace this fragment with the child fragment
        audioChild = new HomeChildFragment(this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.home_wrap_fragment, audioChild);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void handleValueSelected(){
        listItemChoice = IntStream.range(0, selectedItems.length)
                .filter(i -> selectedItems[i])
                .toArray();
    }

=======
>>>>>>> a0bc3779a3f394dc3b1b0dd0c4a0a8381907bc78
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
        if (recordingsDirectory != null && recordingsDirectory.listFiles() != null)
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
    public void playSound(String name) {
        Intent intent = new Intent(getActivity(), ReplayActivity.class);
        intent.putExtra("Name", name);
        intent.setAction("FromHome");
        startActivity(intent);
    }

    @Override
    public void onItemClickConvert(int position) {
        audioList.addAll(getAudioList()); // cập nhật danh sách dữ liệu mới
        homeAudioAdapter.setData(audioList); // đặt lại danh sách dữ liệu cho adapter
        homeAudioAdapter.notifyDataSetChanged(); // thông báo cho adapter biết rằng dữ liệu đã thay đổi và cần phải cập nhật lại
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
        recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/");
        if (recordingsDirectory != null && recordingsDirectory.listFiles() != null)
            size = recordingsDirectory.listFiles().length;
        if (recordingsDirectory.exists()) {
            files = recordingsDirectory.listFiles();
            if (files != null) {
                double tempCapacity = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3") || file.getName().endsWith(".m4a") || file.getName().endsWith(".aac")) {
                        Log.e("file", file.getName());
                        String fileName = file.getName();
                        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
                        tempCapacity += (1.0 * file.length() / (1024 * 1.0));
                        Date lastModifiedDate = new Date(file.lastModified());
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        audioList.add(new Audio(fileName, getFileDuration(file), fileSize, formattedDate, R.drawable.ic_play_outline));
                    }
                }
                if(sumCapacity == 0) {
                    sumCapacity += tempCapacity;
                } else {
                    sumCapacity = tempCapacity;
                }

            }
            audioList.sort(new Comparator<Audio>() {
                @SuppressLint("SimpleDateFormat")
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

    @SuppressLint("DefaultLocale")
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