package com.example.screenshoteasy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;

import com.example.screenshoteasy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class SaveImageHelper {

    public static void saveImageToSdCard(Bitmap bitmap, Context context){
        String[] fileNames =  context.getResources().getStringArray(R.array.fileNames);
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferencesFileName", MODE_PRIVATE);
        String dateFormat = fileNames[ToolsStatusHelper.getFileName(sharedPreferences)];

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        String d = simpleDateFormat.format(date);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ScreenShotEasy");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String fileName = d + ".jpg";
        File file = new File (myDir, fileName);
        if (file.exists ()){
            file.delete ();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
