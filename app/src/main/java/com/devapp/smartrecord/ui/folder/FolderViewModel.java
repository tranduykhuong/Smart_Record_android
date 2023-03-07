package com.devapp.smartrecord.ui.folder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FolderViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public FolderViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is folder fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
