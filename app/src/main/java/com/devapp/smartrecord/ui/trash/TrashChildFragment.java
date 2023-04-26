package com.devapp.smartrecord.ui.trash;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.devapp.smartrecord.databinding.TrashMenuMultiChoiceBinding;

public class TrashChildFragment extends Fragment {

    private DataPassListener mCallback;
    private @NonNull TrashMenuMultiChoiceBinding binding;

    public interface DataPassListener {
        void handleRemoveMultiFolder();
    }

    public TrashChildFragment(){

    }

    public TrashChildFragment(DataPassListener mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = TrashMenuMultiChoiceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        LinearLayout btnRemove = binding.trashDeleteMultiChoice;

        // Add code to configure the child fragment view
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.handleRemoveMultiFolder();
            }
        });

        return root;
    }

}
