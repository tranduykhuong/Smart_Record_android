package com.devapp.smartrecord.services;

import android.media.MediaRecorder;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import com.suman.voice.graphviewlibrary.GraphView;
import com.suman.voice.graphviewlibrary.WaveSample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordingActivity extends AppCompatActivity {
    private static RecordingActivity mInstance = null;
    private final List<WaveSample> pointList = new ArrayList<>();
    private long startTime = 0;
    private Thread mRecordingThread;
    private volatile Boolean stop = false;
    private MediaRecorder mediaRecorder;
    private GraphView graphView;
    File file;
    private String fileName;
    private long longValue[];
    private String fileExt;
    private RecordingActivity() {

//        Log.e(TAG, "RecordingService: " + fileExt);
    }

    public void setFileExt(String mainExt){
        fileExt = mainExt;
    }
    public static RecordingActivity getInstance() {
        if (mInstance == null) {
            mInstance = new RecordingActivity();
        }
        return mInstance;
    }

    public boolean startPlotting(GraphView graphView) {
        if (graphView != null) {
            this.graphView = graphView;
            graphView.setMasterList(pointList);
            graphView.startPlotting();
            return true;
        } else {
            return false;
        }
    }

    public Boolean isRecording() {
        return mRecordingThread != null && mRecordingThread.isAlive();
    }

    public List getSamples() {
        return pointList;
    }

    public List stopRecording() {
        this.stop = true;

        mRecordingThread.interrupt();
        if (graphView != null) {
            graphView.stopPlotting();
        }
        if(mediaRecorder != null)
        {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        longValue = new long[pointList.size()];
        for (int i = 0; i < pointList.size(); i++) {
            if (pointList.get(i) != null) {
                longValue[i] = pointList.get(i).getAmplitude();
            }
        }
        return pointList;
    }

    public List stopWithNoSave(){
        this.stop = true;

        mRecordingThread.interrupt();
        if (graphView != null) {
            graphView.stopPlotting();
        }
        if(mediaRecorder != null)
        {
            mediaRecorder.stop();
            mediaRecorder.release();

            File fileDel = new File(String.valueOf(file.getAbsoluteFile()));
            if (fileDel.exists()) {
                fileDel.delete();
            }
        }
        longValue = new long[pointList.size()];
        for (int i = 0; i < pointList.size(); i++) {
            if (pointList.get(i) != null) {
                longValue[i] = pointList.get(i).getAmplitude();
            }
        }
        return pointList;
    }

    public String getOutputFilePath() {
        String pathFile = null;
        if (file != null)
        {
            pathFile = file.getAbsolutePath();
        }
        return pathFile;
    }

    public void setOutputFilePath(String file) {
        fileName = file;
    }

    public void pauseRecording(){
        if (mediaRecorder != null) {
            graphView.pause();
            mediaRecorder.pause();
        }
    }
    public void resumeRecording(){
        if (mediaRecorder != null) {
            mediaRecorder.resume();
            graphView.startPlotting();
            graphView.resume();
        }
    }

    public void startRecording() {
        this.stop = false;

        if(fileName == null)
        {
            fileName = "Record";
        }

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName + fileExt);
        if(file.exists()) {
            int i = 1;
            while (file.exists())
            {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/" + fileName + " (" + i + ")" + fileExt);
                i++;
            }
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startTime = System.currentTimeMillis();
        } catch (IOException e) {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//            Toast.makeText(getApplicationContext(), "Failed to prepare MediaRecorder: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        pointList.clear();
        mRecordingThread = new Thread(() -> {
            while (!RecordingActivity.this.stop) {
                pointList.add(new WaveSample(System.currentTimeMillis() - startTime, mediaRecorder.getMaxAmplitude()));
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mRecordingThread.start();
    }

    @Override
    public void onDestroy() {
        if(mediaRecorder != null)
            stopRecording();
        mRecordingThread.interrupt();
        graphView.stopPlotting();
        super.onDestroy();
    }
}
