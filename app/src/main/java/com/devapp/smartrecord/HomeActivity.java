package com.devapp.smartrecord;

import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.devapp.smartrecord.api.VoiceToTextActivity;
import com.devapp.smartrecord.api.VoiceToTextResultActivity;
import com.devapp.smartrecord.editmenu.cut.CutActivity;
import com.devapp.smartrecord.services.RecordService;
import com.devapp.smartrecord.ui.alarm.AlarmService;
import com.devapp.smartrecord.ui.alarm.ReminderReceiver;
import com.devapp.smartrecord.ui.folder.FolderFragment;
import com.devapp.smartrecord.soundvariation.VariationActivity;
import com.devapp.smartrecord.ui.home.HomeFragment;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.devapp.smartrecord.databinding.ActivityHomeBinding;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity implements FolderFragment.OnDataPass, HomeFragment.OnDataPass {
    private final int MICRO_PERM_CODE = 101; // MICRO PERMISSION CODE
    private final int SETTING_CODE = 10; // SETTING CODE
    private final int RECORDING_CODE = 11; // SETTING CODE
    private final int EDIT_CODE = 12;

    private ActivityHomeBinding binding;
    private BottomNavigationView navView;
    private ImageView btn_setting, btn_edit;
    private ConfigurationClass config;
    private boolean isEdit = true;

    ActivityResultLauncher<String[]> requestPermissionLauncher;
    private String[] permissionStore = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
    };

    private String[] permissionRecord = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
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
                        int cnt = 0;
                        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                            if (entry.getValue() == true) {
                                cnt++;
                            }
                        }
                        Log.e(TAG, "onActivityResult: " + cnt);
                        if (permissions.containsKey("android.permission.MANAGE_EXTERNAL_STORAGE")) {
                            if (cnt < 3) {
                                askForPermission(permissionStore);
                            } else {
                                permissionRequestCount = 0;
                                onResume();
                                onStop();
                                onRestart();
                                recreate();
                            }
                        } else if (permissions.containsKey("android.permission.RECORD_AUDIO")) {
                            if (permissions.containsValue(false)) {
                                askForPermission(permissionRecord);
                            } else {
                                permissionRequestCount = 0;
                                Intent intent = new Intent(HomeActivity.this, RecordActivity.class);
                                startActivityForResult(intent, RECORDING_CODE);
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                boolean isMarried = sharedPreferences.getBoolean("isRecording", false);

                                if (isMarried == false) {
                                    Intent serviceIntent = new Intent(getApplicationContext(), RecordService.class);
                                    startForegroundService(serviceIntent);
                                }

                                Intent intentRecord = new Intent(HomeActivity.this, RecordActivity1.class);
                                startActivityForResult(intentRecord, RECORDING_CODE);
                            }
                        }
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
            Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivityForResult(intent, SETTING_CODE);
        });

        btn_edit = findViewById(R.id.btn_tool);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean currEdit = isEdit;
                isEdit = !isEdit;
                NavController navController = Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment_activity_main);
                NavDestination currentDestination = navController.getCurrentDestination();
                if (currentDestination != null) {
                    int currentFragmentId = currentDestination.getId();
                    // sử dụng currentFragmentId để xác định fragment hiện tại
                    if (currentFragmentId == R.id.navigation_folder){
                        Bundle args = new Bundle();
                        args.putBoolean("isEdit", currEdit);
                        navController.navigate(R.id.navigation_folder, args);
                        if (currEdit){
                            btn_edit.setImageResource(R.drawable.ic_close);
                        }
                        else{
                            btn_edit.setImageResource(R.drawable.ic_edit);
                        }

                    } else if (currentFragmentId == R.id.navigation_home){
                        Bundle args = new Bundle();
                        args.putBoolean("isEditHome", currEdit);
                        navController.navigate(R.id.navigation_home, args);
                        if (currEdit){
                            btn_edit.setImageResource(R.drawable.ic_close);
                        }
                        else{
                            btn_edit.setImageResource(R.drawable.ic_edit);
                        }

                    }

                    // Xử lý ở các fragment còn lại
                    else{
                    }
                }
            }
        });

        if (SDK_INT >= Build.VERSION_CODES.R) {
            boolean isPermissionGranted = Environment.isExternalStorageManager();
            if (isPermissionGranted) {
                // Quyền đã được cấp
            } else {
                // Quyền chưa được cấp
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 1);
            }
        }

        int grants = 0;
        for (int i=0; i<permissionStore.length; i++) {
            grants += ContextCompat.checkSelfPermission(HomeActivity.this, permissionStore[i]);
        }
        Toast.makeText(this, SDK_INT + ":" + Build.VERSION_CODES.R, Toast.LENGTH_SHORT).show();
        if (SDK_INT > Build.VERSION_CODES.R) {
            if (grants < -3)
                askForPermission(permissionStore);
        } else if (SDK_INT <= Build.VERSION_CODES.R) {
            if (grants < -2)
                askForPermission(permissionStore);
        }

    }

    @Override
    public void onDataPass(boolean data) {
        // Xử lý dữ liệu được truyền từ fragment vào đây
        NavController navController = Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment_activity_main);
        Bundle args = new Bundle();
        args.putBoolean("isEdit", data);
        navController.navigate(R.id.navigation_folder, args);
        btn_edit.setImageResource(R.drawable.ic_edit);
    }

    @Override
    public void onDataPassHome(boolean data) {
        // Xử lý dữ liệu được truyền từ fragment vào đây
        NavController navController = Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment_activity_main);
        Bundle args = new Bundle();
        args.putBoolean("isEdit", data);
        navController.navigate(R.id.navigation_folder, args);
        btn_edit.setImageResource(R.drawable.ic_edit);
    }

    public void navigate_onclick(View view) {
        switch (view.getId()) {
            case R.id.home_btn_record:
            case R.id.folder_btn_record: {
                if (askForPermission(permissionRecord)) {
                    Intent intent = new Intent(HomeActivity.this, RecordActivity.class);
                    startActivityForResult(intent, RECORDING_CODE);
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    boolean isMarried = sharedPreferences.getBoolean("isRecording", false);

                    if (isMarried == false) {
                        Intent serviceIntent = new Intent(getApplicationContext(), RecordService.class);
                        startForegroundService(serviceIntent);
                    }

                    Intent intentRecord = new Intent(HomeActivity.this, RecordActivity1.class);
                    startActivityForResult(intentRecord, RECORDING_CODE);
                }
                break;
            }
        }
    }

//    private boolean askForPermission(String[] permissions) {
//        int grants = 0;
//        for (int i=0; i<permissions.length; i++) {
//            grants += ContextCompat.checkSelfPermission(HomeActivity.this, permissions[i]);
//        }
//        Log.e(TAG, "askForPermission: " + PackageManager.PERMISSION_GRANTED);
//        Log.e(TAG, "askForPermission: "+ grants);
//
//        if (grants != PackageManager.PERMISSION_GRANTED) {
//            boolean rationables = false;
//            for (int i=0; i<permissions.length; i++) {
//                rationables = rationables || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissions[i]);
//            }
//            if (rationables) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
//                builder.setTitle(R.string.grant_permission);
//                builder.setMessage(R.string.list_type);
//                builder.setPositiveButton(R.string.ok, (dialog, which) ->
//                        requestPermissions(permissions)
//                );
//                builder.setNegativeButton(R.string.no, (dialog, which) ->
//                        askForPermission(permissions)
//                );
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            } else {
//                requestPermissions(permissions);
//            }
//            return false;
//        }
//        return true;
//    }

    private int permissionRequestCount = 0;

    private boolean askForPermission(String[] permissions) {
        // Kiểm tra quyền đã được cấp chưa
        int grants = 0;
        for (int i=0; i<permissions.length; i++) {
            grants += ContextCompat.checkSelfPermission(HomeActivity.this, permissions[i]);
        }

        // Nếu quyền chưa được cấp
        if (grants != PackageManager.PERMISSION_GRANTED) {
            permissionRequestCount++;

            // Nếu đây là lần thứ 3 yêu cầu cấp quyền và người dùng vẫn từ chối
            if (permissionRequestCount >= 3) {
                // Gửi thông báo cho người dùng biết rằng ứng dụng cần quyền để hoạt động
                // và hướng dẫn người dùng cấp quyền bằng cách vào mục cài đặt ứng dụng
                Toast.makeText(this, getString(R.string.grant_permission), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                return false;
            }

            // Kiểm tra xem người dùng đã từ chối cấp quyền trước đó hay chưa
            boolean rationables = false;
            for (int i=0; i<permissions.length; i++) {
                rationables = rationables || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissions[i]);
            }

            // Nếu người dùng đã từ chối cấp quyền trước đó
            if (rationables) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(R.string.grant_permission);
                builder.setMessage(R.string.list_type);
                builder.setPositiveButton(R.string.ok, (dialog, which) ->
                        requestPermissions(permissions)
                );
                builder.setNegativeButton(R.string.no, (dialog, which) ->
                        askForPermission(permissions)
                );
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                // Yêu cầu cấp quyền
                requestPermissions(permissions);
            }
            return false;
        }

        // Nếu quyền đã được cấp
        permissionRequestCount = 0;
        return true;
    }


    private void requestPermissions(String[] permissions) {
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

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: Home");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: Home");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: Home");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: Home");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart: Home");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: Home");
    }
}
