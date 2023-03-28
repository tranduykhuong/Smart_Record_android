package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.Manifest;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.devapp.smartrecord.ui.WaveformView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.devapp.smartrecord.databinding.ActivityHomeBinding;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private final int MICRO_PERM_CODE = 101; // MICRO PERMISSION CODE
    private final int SETTING_CODE = 10; // SETTING CODE
    private final int RECORDING_CODE = 11; // SETTING CODE

    private ActivityHomeBinding binding;
    private BottomNavigationView navView;
    private ImageView btn_setting;
    private ConfigurationClass config;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        config = new ConfigurationClass(getApplicationContext());
        Boolean checkConfig = config.getConfig();
        if (!checkConfig){
            config.setConfig(1,"vi",1);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            changeLanguage("vi");
        }
        else{
            config.setTheme();
            changeLanguage(config.getLanguageState());
        }

        // FRAGMENT
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nav_view);
        getSupportActionBar().hide();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_folder, R.id.navigation_alarm, R.id.navigation_trash)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        btn_setting = findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivityForResult(intent, SETTING_CODE);
        });
        askForPermission();
    }

    public void navigate_onclick(View view) {
        switch (view.getId()) {
            case R.id.home_btn_record: {
                Intent intent = new Intent(HomeActivity.this, RecordActivity.class);
                startActivityForResult(intent, RECORDING_CODE);
                break;
            }
            case R.id.folder_btn_record: {
                Intent intent = new Intent(HomeActivity.this, RecordActivity.class);
                startActivityForResult(intent, RECORDING_CODE);
                break;
            }
        }
    }

    private void askForPermission() {
        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.FOREGROUND_SERVICE
        };

        int permissionRecordAudio = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.RECORD_AUDIO);
        int permissionModifyAudio = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionLocation1 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionLocation2 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionLocation3 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        int permissionInternet = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.INTERNET);
        int permissionService = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.FOREGROUND_SERVICE);

        if ((permissionRecordAudio + permissionModifyAudio + permissionCheckRead
                + permissionCheckWrite + permissionLocation1 + permissionLocation2 + permissionLocation3 + permissionInternet)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.INTERNET) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.FOREGROUND_SERVICE)
            ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(R.string.grant_permission);
                builder.setMessage(R.string.list_type);
                builder.setPositiveButton(R.string.ok, (dialog, which) ->
                        ActivityCompat.requestPermissions(HomeActivity.this,
                                permissions,
                                MICRO_PERM_CODE));
                builder.setNegativeButton(R.string.no, (dialog, which) ->
                        askForPermission()
                );
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        MICRO_PERM_CODE);
            }
        }
    }

    private void changeLanguage(String language){
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        android.content.res.Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(language.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
    }
}
