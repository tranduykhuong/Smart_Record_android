package com.devapp.smartrecord.ui.home;

import java.io.Serializable;

public class Audio implements Serializable {

    private String name;
    private String timeOfAudio;
    private String size;
    private String createDate;
    private int image;

    public Audio(String name, String timeOfAudio, String size, String createDate, int image) {
        this.name = name;
        this.timeOfAudio = timeOfAudio;
        this.size = size;
        this.createDate = createDate;
        this.image = image;
    }

    public Audio() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeOfAudio() {
        return timeOfAudio;
    }

    public void setTimeOfAudio(String timeOfAudio) {
        this.timeOfAudio = timeOfAudio;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }


}
