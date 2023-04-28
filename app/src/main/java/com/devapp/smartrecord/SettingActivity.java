package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;


public class SettingActivity extends AppCompatActivity {
    private ConfigurationClass config;
    private LinearLayout btnLanguage;
    private LinearLayout btnFileFormat;
    private TextView twLanguage;
    private TextView twFileFormat;
    private ImageView imgMascot;
    private Switch sw;
    private ImageButton btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().hide();

        config = new ConfigurationClass(getApplicationContext());
        config.getConfig();
        twLanguage = findViewById(R.id.language_text);
        twFileFormat = findViewById(R.id.tv_file_format);

        btnLanguage = findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(view -> {
            showBottomSheetDialogLanguage();
        });
        btnFileFormat = findViewById(R.id.btn_file_format);
        btnFileFormat.setOnClickListener(view -> {
            showBottomSheetDialogFileFormat();
        });

        twLanguage.setText(config.getLanguageState());
        twFileFormat.setText(config.getFileFormat());

        // THEME
        sw = findViewById(R.id.themeSwitch);
        if(config.getThemeMode()==1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sw.setChecked(true);
            sw.setText(R.string.dark);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sw.setChecked(false);
            sw.setText(R.string.light);
        }
        sw.setOnCheckedChangeListener((compoundButton, checked) -> {
            setTheme(checked);
        });

        btnBack = findViewById(R.id.setting_btn_back);
        btnBack.setOnClickListener(view -> this.onBackPressed());

        imgMascot = findViewById(R.id.img_mascot);
        if (config.getThemeMode() == 1) {
            imgMascot.setImageResource(R.drawable.img_mascot_black);
        }
    }

    private void changeLanguage(String language){
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        android.content.res.Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(language.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        config.getConfig();
        config.setConfig(config.getThemeMode(), language, config.getDefaultMode(), config.getFileFormat());
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void showBottomSheetDialogLanguage() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_language);

        bottomSheetDialog.findViewById(R.id.english_setting).setOnClickListener(view -> {
            twLanguage.setText("en");
            changeLanguage("en");
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.findViewById(R.id.vietnamese_setting).setOnClickListener(view -> {
            twLanguage.setText("vi");
            changeLanguage("vi");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showBottomSheetDialogFileFormat() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_type_file_setting);

        bottomSheetDialog.findViewById(R.id.mp3_setting).setOnClickListener(view -> {
            twFileFormat.setText("mp3");
            config.setConfig(config.getThemeMode(),config.getLanguageState(),config.getDefaultMode(), "mp3");
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.findViewById(R.id.m4a_setting).setOnClickListener(view -> {
            twFileFormat.setText("m4a");
            config.setConfig(config.getThemeMode(),config.getLanguageState(),config.getDefaultMode(), "m4a");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void setTheme(boolean checked) {
        if(checked){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            config.setConfig(1,config.getLanguageState(),config.getDefaultMode(), config.getFileFormat());
            sw.setText(R.string.dark);
            sw.setChecked(true);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            config.setConfig(0,config.getLanguageState(),config.getDefaultMode(), config.getFileFormat());
            sw.setText(R.string.light);
            sw.setChecked(false);
        }
        this.recreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
