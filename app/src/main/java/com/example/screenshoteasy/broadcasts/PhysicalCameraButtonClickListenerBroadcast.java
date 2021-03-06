package com.example.screenshoteasy.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.screenshoteasy.ScreenshotApplication;
import com.example.screenshoteasy.activities.TakingScreenShotActivity;
import com.example.screenshoteasy.utils.Utilities;

public class PhysicalCameraButtonClickListenerBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_CAMERA_BUTTON)) {
            Intent intentCapture = new Intent(context, TakingScreenShotActivity.class);
            intentCapture.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ScreenshotApplication.isInBackground) {
                intentCapture.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            context.startActivity(intentCapture);
        }
    }
}
