package com.devapp.smartrecord.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.devapp.smartrecord.databinding.FolderMenuMultiChoiceBinding;

public class HomeChildFragment extends Fragment {
    private DataPassListener mCallback;
    private FolderMenuMultiChoiceBinding binding;

    public interface DataPassListener {
        void handleRemoveMultiFolder();
        void handleMoveMultiFolder(View view);
    }

    public HomeChildFragment(){

    }

    public HomeChildFragment(DataPassListener mCallback) {
        // Required empty public constructor
        this.mCallback = mCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FolderMenuMultiChoiceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        LinearLayout btnRemove = binding.folderDeleteMultiChoice;
        LinearLayout btnMove = binding.folderMoveMultiChoice;


        // Add code to configure the child fragment view
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.handleRemoveMultiFolder();
            }
        });

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.handleMoveMultiFolder(v);
            }
        });

        return root;
    }
}
