package com.devapp.smartrecord.ui.folder;

public class FolderCLassContent {
    private String title;
    private String amount;
    private String size;
    private String hour;
    private int image;

    public FolderCLassContent(String title, String amount, String size, String hour, int image) {
        this.title = title;
        this.amount = amount;
        this.size = size;
        this.hour = hour;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
