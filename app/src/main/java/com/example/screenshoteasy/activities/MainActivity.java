package com.example.screenshoteasy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.example.screenshoteasy.PolicyDialog;
import com.example.screenshoteasy.broadcasts.PhysicalCameraButtonClickListenerBroadcast;
import com.example.screenshoteasy.utils.ToolsStatusHelper;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.utils.Utilities;
import com.example.screenshoteasy.databinding.ActivityMainBinding;
import com.example.screenshoteasy.broadcasts.StopServiceBroadcast;
import com.example.screenshoteasy.services.CreateNotificationService;
import com.example.screenshoteasy.services.FloatingButtonService;
import com.example.screenshoteasy.services.ShakeToTakeScreenShotService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding activityMainBinding;

    private final int REQUEST_CODE_WRITE_STORAGE = 222;
    private final int REQUEST_CODE_OVERLAY = 111;

    private boolean powerStatus, overlayStatus, shakeStatus, cameraStatus, notificationStatus;
    private SharedPreferences sharedPreferencesPower, sharedPreferencesOverlay, sharedPreferencesNotification, sharedPreferencesCamera, sharedPreferencesShake;
    private PhysicalCameraButtonClickListenerBroadcast physicalCameraButtonClickListenerBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        isStoragePermissionGranted();
        doInitialization();
        setMainStatus();
        onClickListener();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Utilities.NOTIFICATION_CHANNEL_ID, Utilities.NOTIFICATION_NAME, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void doInitialization() {
        sharedPreferencesCamera = getSharedPreferences("CameraStatus", MODE_PRIVATE);
        sharedPreferencesNotification = getSharedPreferences("NotificationStatus", MODE_PRIVATE);
        sharedPreferencesOverlay = getSharedPreferences("OverlayStatus", MODE_PRIVATE);
        sharedPreferencesPower = getSharedPreferences("PowerStatus", MODE_PRIVATE);
        sharedPreferencesShake = getSharedPreferences("ShakeStatus", MODE_PRIVATE);
    }

    private void onClickListener() {
        // Image Button Power
        activityMainBinding.imageButtonPower.setOnClickListener(v -> {
            if (!powerStatus) {
                if (isStoragePermissionGranted()) {
                    saveMainStatus();
                    if (!cameraStatus && !notificationStatus && !overlayStatus && !shakeStatus) {
                        Toast.makeText(MainActivity.this, "Please select at least one tool!", Toast.LENGTH_LONG).show();
                    } else {
                        if (cameraStatus) {
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(Intent.ACTION_CAMERA_BUTTON);
                            physicalCameraButtonClickListenerBroadcast = new PhysicalCameraButtonClickListenerBroadcast();
                            registerReceiver(physicalCameraButtonClickListenerBroadcast, intentFilter);
                        }

                        if (shakeStatus) {
                            startService(new Intent(MainActivity.this, ShakeToTakeScreenShotService.class));
                        } else {
                            startService(new Intent(MainActivity.this, CreateNotificationService.class));
                        }

                        if (overlayStatus) {
                            if (!Settings.canDrawOverlays(this)) {
                                new MaterialAlertDialogBuilder(MainActivity.this)
                                        .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:" + getPackageName()));
                                            startActivityForResult(intent, REQUEST_CODE_OVERLAY);
                                        })
                                        .setTitle("No OVERLAY permission")
                                        .setMessage("Go to setting to open permission for SCREENSHOT EASY?")
                                        .show();
                            } else {
                                startService(new Intent(MainActivity.this, FloatingButtonService.class));
                            }
                        }

                        if (ToolsStatusHelper.getStatus(getSharedPreferences("sharedPreferencesStopService", MODE_PRIVATE))) {
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                            StopServiceBroadcast stopServiceBroadcast = new StopServiceBroadcast();
                            registerReceiver(stopServiceBroadcast, intentFilter);

                            stopServiceBroadcast.setScreenOffListener(() -> {
                                unregisterReceiver(stopServiceBroadcast);
                                activityMainBinding.imageButtonPower.setImageResource(R.drawable.stopped);
                            });
                        }

                        powerStatus = true;
                        activityMainBinding.imageButtonPower.setImageResource(R.drawable.started);
                    }
                }
            } else {
                activityMainBinding.imageButtonPower.setImageResource(R.drawable.stopped);
                powerStatus = false;

                if (overlayStatus) {
                    Intent myService = new Intent(MainActivity.this, FloatingButtonService.class);
                    stopService(myService);
                }
                if (shakeStatus) {
                    stopService(new Intent(MainActivity.this, ShakeToTakeScreenShotService.class));
                } else {
                    Intent myService = new Intent(MainActivity.this, CreateNotificationService.class);
                    stopService(myService);
                }
                if (cameraStatus && physicalCameraButtonClickListenerBroadcast != null) {
                    unregisterReceiver(physicalCameraButtonClickListenerBroadcast);
                }
            }
        });

        // Switch Overlay
        activityMainBinding.switchOverLay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (powerStatus) {
                    Intent myService = new Intent(MainActivity.this, FloatingButtonService.class);
                    startService(myService);
                }
                overlayStatus = true;
            } else {
                if (powerStatus) {
                    if(checkToolsStatus(notificationStatus, shakeStatus, cameraStatus)){
                        activityMainBinding.imageButtonPower.performClick();
                    }else{
                        Intent myService = new Intent(MainActivity.this, FloatingButtonService.class);
                        stopService(myService);
                    }
                }
                overlayStatus = false;
            }
        });

        //Switch Notification
        activityMainBinding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (notificationStatus) {
                notificationStatus = false;
            } else {
                notificationStatus = true;
            }

            ToolsStatusHelper.saveStatus(notificationStatus, sharedPreferencesNotification);
            if (powerStatus) {
                if(checkToolsStatus(overlayStatus, shakeStatus, cameraStatus)){
                    activityMainBinding.imageButtonPower.performClick();
                }else{
                    if (!shakeStatus) {
                        stopService(new Intent(MainActivity.this, CreateNotificationService.class));
                        startService(new Intent(MainActivity.this, CreateNotificationService.class));
                    } else {
                        stopService(new Intent(MainActivity.this, ShakeToTakeScreenShotService.class));
                        startService(new Intent(MainActivity.this, ShakeToTakeScreenShotService.class));
                    }
                }
            }
        });

        //Switch Shake
        activityMainBinding.switchShake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (powerStatus) {
                    if(notificationStatus || overlayStatus || cameraStatus){
                        stopService(new Intent(MainActivity.this, CreateNotificationService.class));
                    }
                    startService(new Intent(MainActivity.this, ShakeToTakeScreenShotService.class));
                }
                shakeStatus = true;
            } else {
                if (powerStatus) {
                    if(checkToolsStatus(overlayStatus, notificationStatus, cameraStatus)){
                        activityMainBinding.imageButtonPower.performClick();
                    }else{
                        stopService(new Intent(MainActivity.this, ShakeToTakeScreenShotService.class));
                        stopService(new Intent(MainActivity.this, CreateNotificationService.class));
                        startService(new Intent(MainActivity.this, CreateNotificationService.class));
                    }
                }
                shakeStatus = false;
            }
        });

        //Switch Camera
        activityMainBinding.switchCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cameraStatus = true;
            } else {
                if(powerStatus){
                    if(checkToolsStatus(overlayStatus, notificationStatus, shakeStatus)){
                        activityMainBinding.imageButtonPower.performClick();
                    }
                }
                cameraStatus = false;
            }
        });

        activityMainBinding.imageButtonGallery.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        activityMainBinding.imageButtonPolicy.setOnClickListener(v -> {
            PolicyDialog policyDialog = new PolicyDialog();
            policyDialog.show(getSupportFragmentManager(), "policyDialog");
        });

        activityMainBinding.imageButtonSetting.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));
    }

    private boolean checkToolsStatus(boolean stt1, boolean stt2, boolean stt3){
        return !stt1 && !stt2 && !stt3;
    }

    private boolean isStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            switch (requestCode) {
                case REQUEST_CODE_WRITE_STORAGE:
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(intent, REQUEST_CODE_WRITE_STORAGE);
                            })
                            .setTitle("No STORAGE permission")
                            .setMessage("Go to setting to open permission for SCREENSHOT EASY?")
                            .show();
                    break;
                case REQUEST_CODE_OVERLAY:
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_CODE_OVERLAY);
                            })
                            .setTitle("No OVERLAY permission")
                            .setMessage("Go to setting to open permission for SCREENSHOT EASY?")
                            .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (!Settings.canDrawOverlays(this)) {
                activityMainBinding.imageButtonPower.performClick();
                ToolsStatusHelper.saveStatus(false, sharedPreferencesPower);
            } else {
                startService(new Intent(MainActivity.this, FloatingButtonService.class));
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setMainStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveMainStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.isAppOnForeGround = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMainStatus();
        Utilities.isAppOnForeGround = false;
    }

    private void setMainStatus() {
        powerStatus = ToolsStatusHelper.getStatus(sharedPreferencesPower);
        cameraStatus = ToolsStatusHelper.getStatus(sharedPreferencesCamera);
        notificationStatus = ToolsStatusHelper.getStatus(sharedPreferencesNotification);
        shakeStatus = ToolsStatusHelper.getStatus(sharedPreferencesShake);
        overlayStatus = ToolsStatusHelper.getStatus(sharedPreferencesOverlay);

        if (powerStatus) {
            activityMainBinding.imageButtonPower.setImageResource(R.drawable.started);
        }
        if (cameraStatus) {
            activityMainBinding.switchCamera.setChecked(true);
        }
        if (shakeStatus) {
            activityMainBinding.switchShake.setChecked(true);
        }
        if (notificationStatus) {
            activityMainBinding.switchNotification.setChecked(true);
        }
        if (overlayStatus) {
            activityMainBinding.switchOverLay.setChecked(true);
        }
    }

    private void saveMainStatus() {
        ToolsStatusHelper.saveStatus(powerStatus, sharedPreferencesPower);
        ToolsStatusHelper.saveStatus(cameraStatus, sharedPreferencesCamera);
        ToolsStatusHelper.saveStatus(overlayStatus, sharedPreferencesOverlay);
        ToolsStatusHelper.saveStatus(shakeStatus, sharedPreferencesShake);
        ToolsStatusHelper.saveStatus(notificationStatus, sharedPreferencesNotification);
    }
}