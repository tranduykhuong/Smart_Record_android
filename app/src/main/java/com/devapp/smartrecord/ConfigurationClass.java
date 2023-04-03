package com.devapp.smartrecord;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ConfigurationClass {
    final String filename = "config.txt";
    private Context context;
    private int isDarkMode;
    private String language;
    private String fileFormat;
    private int isDefault;

    ConfigurationClass(Context a) {
        context = a;
    }
    public int getThemeMode() {
        return isDarkMode;
    }
    public String getLanguageState() {
        return language;
    }
    public int getDefaultMode(){
        return isDefault;
    }
    public String getFileFormat(){
        return fileFormat;
    }

    protected void setTheme() {
        if(isDarkMode == 1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setConfig(1,language,isDefault, fileFormat);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setConfig(0,language,isDefault, fileFormat);
        }
    }

    protected boolean getConfig(){
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            isDarkMode = Integer.parseInt(line);
            language = reader.readLine();
            line = reader.readLine();
            isDefault = Integer.parseInt(line);
            fileFormat = reader.readLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected void setConfig(int Theme, String Lan,int Default, String file) {
        try {
            FileOutputStream fout = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fout.write((String.valueOf(Theme) + '\n').getBytes());
            fout.write((Lan + "\n").getBytes());
            fout.write((String.valueOf(Default) + '\n').getBytes());
            fout.write((file + "\n").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
