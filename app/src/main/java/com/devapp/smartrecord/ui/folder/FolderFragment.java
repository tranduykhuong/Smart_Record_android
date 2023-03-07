package com.devapp.smartrecord.ui.folder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.devapp.smartrecord.databinding.FragmentFolderBinding;
import com.devapp.smartrecord.databinding.FragmentHomeBinding;

public class FolderFragment extends Fragment {
    private FragmentFolderBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FolderViewModel folderViewModel =
                new ViewModelProvider(this).get(FolderViewModel.class);

        binding = FragmentFolderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textFolder;
        folderViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
