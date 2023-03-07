package com.devapp.smartrecord.ui.alarm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AlarmViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public AlarmViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is alarm fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
