package com.example.screenshoteasy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    ImageButton imgButtonBack;
    LinearLayout layoutStopService, layoutSaveSilently, layoutAutoStart, layoutFileName;
    MaterialCheckBox checkBoxAutoStart, checkBoxSaveSilently, checkBoxStopService;
    TextView txtViewFileName;

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
            }else{
                isAutoStart = true;
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
    protected void onDestroy() {
        super.onDestroy();
        ToolsStatusHelper.saveStatus(isSaveSilently, sharedPreferencesSaveSilently);
        ToolsStatusHelper.saveStatus(isAutoStart, sharedPreferencesAutoStart);
        ToolsStatusHelper.saveStatus(isStopService, sharedPreferencesStopService);
        ToolsStatusHelper.saveFileName(fileNamePosition, sharedPreferencesFileName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utilities.isAppOnForeGround = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.isAppOnForeGround = true;
    }
}
