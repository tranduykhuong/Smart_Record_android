package com.devapp.smartrecord.ui.folder;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.HomeActivity;
import com.devapp.smartrecord.R;
import com.devapp.smartrecord.SettingActivity;
import com.devapp.smartrecord.databinding.FragmentFolderBinding;
import com.devapp.smartrecord.ui.home.HomeFragment;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FolderFragment extends Fragment {
    private FragmentFolderBinding binding;
    private RecyclerView rcvFolder;
    private FolderClassContentAdapter adapterFolder;
    private ArrayList<FolderCLassContent> listFolder;
    private List<FolderCLassContent> searchListFolder = new ArrayList<>();


    private boolean mSortByNameAscending = true;
    private Double sizeFolder = 0.0;
    private boolean isFirstPrivate = true;

    private boolean mSortByNameAscending = true;
    private int amountFolder = 0;
    private int sizeFolder = 0;

    public List<FolderCLassContent> sortList(List<FolderCLassContent> listFolder, String sortBy, boolean ascending) {
        if (sortBy.equals("name")) {
            Collections.sort(listFolder, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent folder2) {
                    return folder1.getTitle().compareToIgnoreCase(folder2.getTitle());
                }
            });
        } else if (sortBy.equals("size")) {
            Collections.sort(listFolder, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent folder2) {
                    if (Integer.parseInt(folder1.getSize().split(" ")[0]) == Integer.parseInt(folder2.getSize().split(" ")[0])) {
                        return 0;
                    } else if (Integer.parseInt(folder1.getSize().split(" ")[0]) > Integer.parseInt(folder2.getSize().split(" ")[0])) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        } else if (sortBy.equals("day")) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Collections.sort(listFolder, new Comparator<FolderCLassContent>() {
                @Override
                public int compare(FolderCLassContent folder1, FolderCLassContent folder2) {
                    Date date1 = null, date2 = null;
                    try {
                        date1 = format.parse(folder1.getHour());
                        date2 = format.parse(folder2.getHour());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date1 != null && date2 != null) {
                        return date1.compareTo(date2); // sắp xếp giảm dần
                    } else {
                        return 0;
                    }
                }
            });
        }

        if (!ascending) {
            Collections.reverse(listFolder);
        }

        return listFolder;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FolderViewModel folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        binding = FragmentFolderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textFolder;
        final LinearLayout menuFilter = binding.folderWrapDay;
        final TextView folderFilterTitle = binding.folderDay;
        final SearchView searchView = binding.searchFolderEdt;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String searchText = newText.toLowerCase().trim();
                List<FolderCLassContent> filteredList = new ArrayList<>();

                for (FolderCLassContent item : listFolder) {
                    String itemText = item.getTitle().toLowerCase();
                    if (itemText.contains(searchText)) {
                        filteredList.add(item);
                    }
                }
                adapterFolder.filterList(filteredList);
                return true;
            }
        });



        menuFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.folder_context_filter_menu, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                // handle button clicks
                TextView filterTitle = popupView.findViewById(R.id.folder_filter_item_title);
                TextView filterDay = popupView.findViewById(R.id.folder_filter_item_day);
                TextView filterName = popupView.findViewById(R.id.folder_filter_item_name);
                TextView filterSize = popupView.findViewById(R.id.folder_filter_item_size);
                TextView filterDestroy = popupView.findViewById(R.id.folder_filter_item_destroy);

                filterTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.update();
                    }
                });

                filterDay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterDay.getText());

                        List<FolderCLassContent> sortedList = sortList(getListFolder(),"day", mSortByNameAscending);
                        adapterFolder.updateList(sortedList);

                        popupWindow.dismiss();
                    }
                });

                filterName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterName.getText());

                        List<FolderCLassContent> sortedList = sortList(getListFolder(),"name", mSortByNameAscending);
                        adapterFolder.updateList(sortedList);

                        popupWindow.dismiss();
                    }
                });

                filterSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterSize.getText());

                        List<FolderCLassContent> sortedList = sortList(getListFolder(),"size", mSortByNameAscending);
                        adapterFolder.updateList(sortedList);

                        popupWindow.dismiss();
                    }
                });

                filterDestroy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

        final TextView totalFolder, totalSizeFolder;

        rcvFolder = binding.folderRcvList;
        totalFolder = binding.folderAmount;
        totalSizeFolder = binding.folderCapacity;
        rcvFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        listFolder = new ArrayList<>();
        listFolder = (ArrayList<FolderCLassContent>) getListFolder();
        adapterFolder = new FolderClassContentAdapter(getContext());

        List<FolderCLassContent> sortedList = sortList(getListFolder(),"day", mSortByNameAscending);

        totalFolder.setText(sortedList.size()+"");
        totalSizeFolder.setText(sizeFolder+"");
        adapterFolder.setData(sortedList);
        rcvFolder.setAdapter(adapterFolder);

        final ImageButton btnRecord = binding.folderBtnRecord;
        final ImageButton btnMode = binding.folderIconCvup;
        final LinearLayout layoutWrapRecord = binding.folderWrapRecord;
        final RelativeLayout privateFolder = binding.folderItemPrivateRow;

        privateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.folder_context_password_private, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT ;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 100);
            }
        });
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutWrapRecord.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));
            }
        });
//        folderViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @NonNull
    private List<FolderCLassContent> getListFolder() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        List<FolderCLassContent> folderList = new ArrayList<>();
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory().toString());

        if (recordingsDirectory.exists()) {
            File[] folders = recordingsDirectory.listFiles();
            if (folders != null) {
                for (File folder : folders) {
                    int amount = 0;
                    long size = 0;
                    if (folder.isDirectory()) {
                        File[] files = folder.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile()) {
                                    amount = amount + 1;
                                    size = size + file.length();
                                }
                            }
                        }
                        String fileName = folder.getName();
                        String amountFileOfFolder = String.valueOf(amount);
                        String fileSize = decimalFormat.format(1.0 * size / 1024);
                        Date lastModifiedDate = new Date(folder.lastModified());
                        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastModifiedDate);
                        folderList.add(new FolderCLassContent(fileName, amountFileOfFolder + " bản ghi - ",fileSize+" MB", formattedDate, R.drawable.ic_pink500_folder));
                    }
                }
            }
        }
        return folderList;
    }
}
