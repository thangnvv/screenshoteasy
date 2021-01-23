package com.example.screenshoteasy.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.screenshoteasy.broadcasts.StartOnBootBroadcast;
import com.example.screenshoteasy.utils.ToolsStatusHelper;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.utils.Utilities;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferencesSaveSilently, sharedPreferencesAutoStart, sharedPreferencesFileName, sharedPreferencesStopService;
    private boolean isSaveSilently;
    private boolean isAutoStart;
    private boolean isStopService;
    private int fileNamePosition;
    private String[] fileNameArray;
    private final StartOnBootBroadcast startOnBootBroadcast = new StartOnBootBroadcast();

    private ImageButton imgButtonBack;
    private LinearLayout layoutStopService, layoutSaveSilently, layoutAutoStart, layoutFileName;
    private MaterialCheckBox checkBoxAutoStart, checkBoxSaveSilently, checkBoxStopService;
    private TextView txtViewFileName;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        doInitialization();
        setOption();
        setOnClick();
    }

    private void setOption() {
        isSaveSilently = ToolsStatusHelper.getStatus(sharedPreferencesSaveSilently);
        isAutoStart = ToolsStatusHelper.getStatus(sharedPreferencesAutoStart);
        isStopService = ToolsStatusHelper.getStatus(sharedPreferencesStopService);
        fileNamePosition = ToolsStatusHelper.getFileName(sharedPreferencesFileName);

        txtViewFileName.setText(fileNameArray[fileNamePosition]);
        checkBoxSaveSilently.setChecked(isSaveSilently);
        checkBoxStopService.setChecked(isStopService);
        checkBoxAutoStart.setChecked(isAutoStart);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setOnClick() {
        imgButtonBack.setOnClickListener(v -> {
            Intent intentGoMain = new Intent(SettingActivity.this, MainActivity.class);
            intentGoMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentGoMain);
        });

        layoutStopService.setOnClickListener(v ->{
            if(isStopService){
                isStopService = false;
            }else{
                isStopService = true;
            }
            checkBoxStopService.setChecked(isStopService);
        });

        layoutSaveSilently.setOnClickListener(v -> {
            if(isSaveSilently){
                isSaveSilently = false;
            }else{
                isSaveSilently = true;
            }
            checkBoxSaveSilently.setChecked(isSaveSilently);
        });

        layoutAutoStart.setOnClickListener(v -> {
            if(isAutoStart){
                isAutoStart = false;
                try{
                    unregisterReceiver(startOnBootBroadcast);
                    Log.d("DDD", "UnRegister Broadcast succeed");
                }catch (RuntimeException e){
                    Log.d("DDD", "UnRegister Broadcast failed" + e.getMessage());
                }
            }else{
                isAutoStart = true;
                IntentFilter filterBootCompleted = new IntentFilter();
                filterBootCompleted.addAction(Intent.ACTION_BOOT_COMPLETED);
                registerReceiver(startOnBootBroadcast, filterBootCompleted);
                Log.d("DDD", "On Register Broadcast" );
            }
            checkBoxAutoStart.setChecked(isAutoStart);
        });

        layoutFileName.setOnClickListener(v -> {

            new MaterialAlertDialogBuilder(SettingActivity.this)
                    .setTitle("Select Filename")
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("OK", (dialog, which) -> {
                        txtViewFileName.setText(fileNameArray[fileNamePosition]);
                    })
                    .setSingleChoiceItems(fileNameArray, fileNamePosition, (dialog, which) -> {
                        fileNamePosition = which;
                    })
                    .show();
        });
    }

    private void doInitialization() {
        layoutStopService = findViewById(R.id.layoutStopService);
        layoutSaveSilently = findViewById(R.id.layoutSaveSilently);
        layoutAutoStart = findViewById(R.id.layoutAutoStart);
        layoutFileName = findViewById(R.id.layoutFileName);
        checkBoxAutoStart = findViewById(R.id.checkBoxAutoStart);
        checkBoxSaveSilently = findViewById(R.id.checkBoxSaveSilently);
        checkBoxStopService = findViewById(R.id.checkBoxStopService);
        txtViewFileName = findViewById(R.id.textViewFileName);
        imgButtonBack = findViewById(R.id.imageButtonBack);

        sharedPreferencesAutoStart = getSharedPreferences("sharedPreferencesAutoStart", MODE_PRIVATE);
        sharedPreferencesSaveSilently = getSharedPreferences("sharedPreferencesSaveSilently", MODE_PRIVATE);
        sharedPreferencesFileName = getSharedPreferences("sharedPreferencesFileName", MODE_PRIVATE);
        sharedPreferencesStopService = getSharedPreferences("sharedPreferencesStopService", MODE_PRIVATE);

        fileNameArray = getResources().getStringArray(R.array.fileNames);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ToolsStatusHelper.saveStatus(isSaveSilently, sharedPreferencesSaveSilently);
        ToolsStatusHelper.saveStatus(isAutoStart, sharedPreferencesAutoStart);
        ToolsStatusHelper.saveStatus(isStopService, sharedPreferencesStopService);
        ToolsStatusHelper.saveFileName(fileNamePosition, sharedPreferencesFileName);
    }
}
