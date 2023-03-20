package com.devapp.smartrecord.ui.folder;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devapp.smartrecord.R;
import com.devapp.smartrecord.databinding.FragmentFolderBinding;
import com.devapp.smartrecord.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment {
    private FragmentFolderBinding binding;
    private RecyclerView rcvFolder;
    private FolderClassContentAdapter adapterFolder;
    private ArrayList<FolderCLassContent> listFolder;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FolderViewModel folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        binding = FragmentFolderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textFolder;
        final LinearLayout menuFilter = binding.folderWrapDay;
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

                TextView folderFilterTitle = binding.folderDay;
                // handle button clicks
                TextView filterTitle = popupView.findViewById(R.id.folder_filer_item_title);
                TextView filterDay = popupView.findViewById(R.id.folder_filer_item_day);
                TextView filterName = popupView.findViewById(R.id.folder_filer_item_name);
                TextView filterSize = popupView.findViewById(R.id.folder_filer_item_size);
                TextView filterDestroy = popupView.findViewById(R.id.folder_filer_item_destroy);

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
                        popupWindow.dismiss();
                    }
                });

                filterName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterName.getText());
                        popupWindow.dismiss();
                    }
                });

                filterSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        folderFilterTitle.setText(filterSize.getText());
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

        rcvFolder = binding.folderRcvList;
        rcvFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        listFolder = new ArrayList<>();
        adapterFolder = new FolderClassContentAdapter(getContext());
        adapterFolder.setData(getListFolder());
        rcvFolder.setAdapter(adapterFolder);

//        folderViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<FolderCLassContent> getListFolder() {
        listFolder.add(new FolderCLassContent("Thư mục riêng tư", "Bí mật", "","", R.drawable.ic_private_pink500_folder));
        listFolder.add(new FolderCLassContent("Đại học khoa học tự nhiên", "2 bản ghi - ", "10MB", "Hôm nay 16:06", R.drawable.ic_pink500_folder));
        listFolder.add(new FolderCLassContent("Đại học khoa học tự nhiên", "2 bản ghi - ", "10MB", "Hôm nay 16:06", R.drawable.ic_pink500_folder));

        return listFolder;
    }

}
