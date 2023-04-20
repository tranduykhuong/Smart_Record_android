package com.suman.voice.graphviewlibrary;
public class WaveSample {
    private long time;
    private long amplitude;

    public WaveSample(long time, int amplitude) {
        this.time = time;
        this.amplitude = amplitude;
    }

    public long getTime() {
        return time;
    }

    public long getAmplitude() {
        return amplitude;
    }

}

