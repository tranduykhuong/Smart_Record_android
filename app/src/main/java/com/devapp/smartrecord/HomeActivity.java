package com.devapp.smartrecord;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.devapp.smartrecord.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private final int MICRO_PERM_CODE = 101; // MICRO PERMISSION CODE

    private ActivityHomeBinding binding;
    private BottomNavigationView navView;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

//        askForPermission();
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
                Manifest.permission.INTERNET
        };

        int permissionRecordAudio = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.RECORD_AUDIO);
        int permissionModifyAudio = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionLocation1 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionLocation2 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionLocation3 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        int permissionInternet = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.INTERNET);

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
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.INTERNET)
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
}
