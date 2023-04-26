package com.devapp.smartrecord.ui.trash;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.ReplayActivity;
import com.devapp.smartrecord.databinding.FragmentTrashBinding;
import com.devapp.smartrecord.ui.folder.FolderCLassContent;
import com.devapp.smartrecord.ui.home.Audio;
import com.devapp.smartrecord.ui.home.HomeChildFragment;
import com.devapp.smartrecord.ui.home.HomeFragment;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TrashFragment extends Fragment implements TrashAdapter.OnItemClickListenerTrash, TrashChildFragment.DataPassListener{
    private TrashAdapter trashAdapter;
    private List<Item> itemList;
    private File trashDirectory;
    private TextView totalCapacityItem;
    private TextView totalAmountAudio;
    private int[] listItemChoice;
    private boolean[] selectedItems;
    private ImageView imageTotalChoice;
    private TextView capacityUnit, textAudio;
    private double sumCapacity = 0;
    private TextView trashSelectAll;
    private FragmentTrashBinding binding;
    private boolean isEdit, isTotalChecked = false;
    private int size;
    private TrashChildFragment audioChild;
    private OnDataPass dataPasser;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TrashViewModel trashViewModel = new ViewModelProvider(this).get(TrashViewModel.class);
        binding = FragmentTrashBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        totalCapacityItem = binding.trashCapacityAudio;
        totalAmountAudio = binding.trashAmountAudio;
        capacityUnit = binding.capacityAudioUnit;
        textAudio = binding.trashTitleAudio;
        trashSelectAll = binding.trashSelectAll;

        SearchView searchView = binding.searchViewAudioTrash;
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        // Lấy dữ liệu được truyền vào từ activity
        Bundle args = getArguments();
        if (args != null){
            isEdit = args.getBoolean("isEditTrash");
        }

        RecyclerView recyclerView = binding.trashRcvAudioList;
        recyclerView.setLayoutManager(new LinearLayoutManager((getContext())));
        trashAdapter = new TrashAdapter(getContext(), this);

        trashAdapter.setDataItem(getItemList());
        recyclerView.setAdapter(trashAdapter);

        showMultiFolder();

        if (trashDirectory != null && trashDirectory.listFiles() != null)
            totalAmountAudio.setText(String.valueOf(Objects.requireNonNull(trashDirectory.listFiles()).length));
        if(sumCapacity >= 1024) {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity));
        }

        return root;
    }

    public interface OnDataPass {
        void onDataPassHome(boolean data);
    }
    @NonNull
    private List<Item> getItemList() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        itemList = new ArrayList<>();
        trashDirectory = new File(Environment.getExternalStorageDirectory().toString()+"/Recordings/TrashAudio/");
        if (!trashDirectory.exists()) {
            trashDirectory.mkdir();
        }
        Log.e("sdnfsdhfk", String.valueOf(trashDirectory));
        if (trashDirectory != null && trashDirectory.listFiles() != null)
            size = trashDirectory.listFiles().length;

        trashDirectory.listFiles();

        if (trashDirectory.exists()) {
            File[] files = trashDirectory.listFiles();
            if (files != null) {
                double tempCapacity = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3") || file.getName().endsWith(".aac") || file.getName().endsWith(".m4a")) {
                        String fileName = file.getName();
                        String fileSize = decimalFormat.format(1.0 * file.length() / 1024);
                        tempCapacity += (1.0 * file.length() / (1024 * 1.0));
                        Date lastModifiedDate = new Date(file.lastModified());
                        @SuppressLint("SimpleDateFormat") String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        itemList.add(new Item(fileName, getFileDuration(file), fileSize, formattedDate, R.drawable.ic_play_outline));
                    }
                }
                if(sumCapacity == 0) {
                    sumCapacity += tempCapacity;
                } else {
                    sumCapacity = tempCapacity;
                }

            }
        }
        return itemList;
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

    private void filterList(String text) {
        List<Item> filterList = new ArrayList<>();
        for (Item _item : itemList) {
            if (_item.getName().toLowerCase().contains(text.toLowerCase())){
                filterList.add(_item);
            }
        }
        if (filterList.isEmpty()) {
            trashAdapter.setDataItem(null);
            Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_LONG).show();
        }else {
            trashAdapter.setDataItem(filterList);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemClick(int position) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (trashDirectory != null && trashDirectory.listFiles() != null)
            totalAmountAudio.setText(String.valueOf(Objects.requireNonNull(trashDirectory.listFiles()).length));
        if(sumCapacity >= 1024) {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity / (1.0 * 1024)));
            capacityUnit.setText("MB");
        }
        else {
            totalCapacityItem.setText(decimalFormat.format(sumCapacity));
        }
    }

    @Override
    public void playSound(String name, int position) {

        if(isEdit){
            if (isEdit){
                if (selectedItems[position]){
                    itemList.get(position).setImage(R.drawable.ic_circle_folder);
                    trashAdapter.notifyDataSetChanged();
                    selectedItems[position] = false;
                }
                else {
                    itemList.get(position).setImage(R.drawable.ic_circle_checked_folder);
                    trashAdapter.notifyDataSetChanged();
                    selectedItems[position] = true;
                }
            }
        }
        else{
            Intent intent = new Intent(getActivity(), ReplayActivity.class);
            intent.putExtra("NameTrash", name);
            intent.setAction("FromTrash");
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Method để gửi dữ liệu về Activity
    public void passData(boolean isEdit) {
        if (dataPasser != null)
            dataPasser.onDataPassHome(isEdit);
    }

    private void showMultiFolder(){
        if (isEdit) {
            totalCapacityItem.setVisibility(View.GONE);
            capacityUnit.setVisibility(View.GONE);
            totalAmountAudio.setVisibility(View.GONE);
            textAudio.setVisibility(View.GONE);

            imageTotalChoice = binding.trashTotalChoice;
            imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
            trashSelectAll.setText("Chọn tất cả");

//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoLayout.getLayoutParams();
            RelativeLayout.LayoutParams paramsText = (RelativeLayout.LayoutParams) totalAmountAudio.getLayoutParams();
//            params.leftMargin = 30;
            paramsText.leftMargin = 14;

            imageTotalChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isTotalChecked) {
                        imageTotalChoice.setImageResource(R.drawable.ic_circle_checked_folder);
                        for (int i = 0; i < itemList.size(); i++) {
                            itemList.get(i).setImage(R.drawable.ic_circle_checked_folder);
                        }
                        trashAdapter.notifyDataSetChanged();
                        Arrays.fill(selectedItems, true);
                        isTotalChecked = true;
                    } else {
                        imageTotalChoice.setImageResource(R.drawable.ic_circle_folder);
                        for (int i = 0; i < itemList.size(); i++) {
                            itemList.get(i).setImage(R.drawable.ic_circle_folder);
                        }
                        trashAdapter.notifyDataSetChanged();
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
        if (itemList != null){
            for (int i = 0; i < itemList.size(); i++){
                itemList.get(i).setImage(R.drawable.ic_circle_folder);
            }
            trashAdapter.notifyDataSetChanged();
        }
    }

    private void showChildFragment() {
        // Replace this fragment with the child fragment
        audioChild = new TrashChildFragment(this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.trash_wrap_fragment, audioChild);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void handleValueSelected(){
        listItemChoice = IntStream.range(0, selectedItems.length)
                .filter(i -> selectedItems[i])
                .toArray();
    }

    @Override
    public void handleRemoveMultiFolder() {
        handleValueSelected();
        if (listItemChoice.length == 0){
            Toast.makeText(getActivity(), getView().getContext().getText(R.string.announce_notify_warning_len_null), Toast.LENGTH_SHORT).show();

            return;
        }
        //Tạo ra dialog để xác nhận xóa hay không
        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setMessage(getView().getContext().getString(R.string.question_delete));
        builder.setPositiveButton(getView().getContext().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {
                int count = 0;
                for (int i = 0; i < listItemChoice.length; i++) {
                    if (i != 0){
                        listItemChoice[i] -= count;
                    }
                    Item folder = itemList.get(listItemChoice[i]);
                    String folderName = folder.getName();
                    File sourceFile  = new File(Environment.getExternalStorageDirectory().toString() + "/Recordings/TrashAudio/" + folderName); // Lấy đường dẫn đầy đủ đến tệp

                    try {
                        itemList.remove(listItemChoice[i]);
                        trashAdapter.notifyItemRemoved(listItemChoice[i]);
                        Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_successfully), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), getView().getContext().getString(R.string.announce_moved_unsuccessfully) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    count++;
                }
                listItemChoice = null;
                selectedItems = new boolean[itemList.size()];
                Arrays.fill(selectedItems, false);
                trashAdapter.notifyDataSetChanged();
                passData(false);
            }
        });
        builder.setNegativeButton(getView().getContext().getString(R.string.answer_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

}
