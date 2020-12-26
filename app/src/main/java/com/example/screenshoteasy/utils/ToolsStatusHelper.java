package com.example.screenshoteasy.utils;

import android.content.SharedPreferences;

public class ToolsStatusHelper {

    public static void saveStatus(Boolean status, SharedPreferences sharedPreferences){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("status", status);
            editor.apply();
    }

    public static Boolean getStatus(SharedPreferences sharedPreferences){
        return sharedPreferences.getBoolean("status", false);
    }

    public static int getFileName(SharedPreferences sharedPreferences){
        return sharedPreferences.getInt("filePosition", 0);
    }

    public static void saveFileName(int position, SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("filePosition", position);
        editor.apply();
    }
}
