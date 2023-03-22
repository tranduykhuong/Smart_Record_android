package com.devapp.smartrecord.ui.alarm;

public class ItemClassContent {
    private String title;
    private String time;
    private boolean checked;

    public ItemClassContent(String title, String time, boolean checked) {
        this.title = title;
        this.time = time;
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setchecked(boolean checked) {
        this.checked = checked;
    }
}
