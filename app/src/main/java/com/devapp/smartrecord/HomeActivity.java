package com.devapp.smartrecord;

import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.devapp.smartrecord.api.VoiceToTextActivity;
import com.devapp.smartrecord.api.VoiceToTextResultActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.devapp.smartrecord.databinding.ActivityHomeBinding;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    private final int MICRO_PERM_CODE = 101; // MICRO PERMISSION CODE
    private final int SETTING_CODE = 10; // SETTING CODE
    private final int RECORDING_CODE = 11; // SETTING CODE

    private ActivityHomeBinding binding;
    private BottomNavigationView navView;
    private ImageView btn_setting;
    private ConfigurationClass config;

    ActivityResultLauncher<String[]> requestPermissionLauncher;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
    };

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> permissions) {
                        // Xử lý kết quả trả về từ việc yêu cầu cấp quyền
                    }
                });

        config = new ConfigurationClass(getApplicationContext());
        Boolean checkConfig = config.getConfig();
        if (!checkConfig){
            config.setConfig(0,"vi",1, "mp3");
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
            Intent intent = new Intent(HomeActivity.this, VoiceToTextActivity.class);
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
//            case R.id.record_btn_back: {
//                Intent intent = new Intent(RecordActivity.class, ReplayActivity.class);
//                startActivityForResult(intent, RECORDING_CODE);
//                break;
//            }
        }
    }

    private void askForPermission() {
        int grants = 0;
        for (int i=0; i<permissions.length; i++) {
            grants += ContextCompat.checkSelfPermission(HomeActivity.this, permissions[i]);
        }

        if (grants != PackageManager.PERMISSION_GRANTED) {
            boolean rationables = false;
            for (int i=0; i<permissions.length; i++) {
                rationables = rationables || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissions[i]);
            }
            if (rationables) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(R.string.grant_permission);
                builder.setMessage(R.string.list_type);
                builder.setPositiveButton(R.string.ok, (dialog, which) ->
                        requestPermissions()
                );
                builder.setNegativeButton(R.string.no, (dialog, which) ->
                        askForPermission()
                );
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                requestPermissions();
            }
        }
    }

    private void requestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            requestPermissionLauncher.launch(permissions);
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    MICRO_PERM_CODE);
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
