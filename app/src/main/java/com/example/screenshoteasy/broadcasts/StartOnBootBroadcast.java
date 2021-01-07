package com.example.screenshoteasy.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.example.screenshoteasy.services.CreateNotificationService;
import com.example.screenshoteasy.services.FloatingButtonService;
import com.example.screenshoteasy.services.ShakeToTakeScreenShotService;
import com.example.screenshoteasy.utils.ToolsStatusHelper;

import static android.content.Context.MODE_PRIVATE;

public class StartOnBootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                SharedPreferences sharedPreferencesOverlay = context.getSharedPreferences("OverlayStatus", MODE_PRIVATE);
                SharedPreferences sharedPreferencesShake = context.getSharedPreferences("ShakeStatus", MODE_PRIVATE);
                SharedPreferences sharedPreferencesNotification = context.getSharedPreferences("NotificationStatus", MODE_PRIVATE);
                SharedPreferences sharedPreferencesCamera = context.getSharedPreferences("CameraStatus", MODE_PRIVATE);
                SharedPreferences sharedPreferencesPower = context.getSharedPreferences("PowerStatus", MODE_PRIVATE);

                boolean statusOverLay = ToolsStatusHelper.getStatus(sharedPreferencesOverlay);
                boolean statusShake = ToolsStatusHelper.getStatus(sharedPreferencesShake);
                boolean statusCamera = ToolsStatusHelper.getStatus(sharedPreferencesCamera);
                boolean statusNotification = ToolsStatusHelper.getStatus(sharedPreferencesNotification);

                if (statusCamera || statusNotification || statusOverLay || statusShake) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        if (statusShake) {
                            context.startForegroundService(new Intent(context, ShakeToTakeScreenShotService.class));
                        } else {
                            context.startForegroundService(new Intent(context, CreateNotificationService.class));
                        }

                        if (statusOverLay) {
                            context.startForegroundService(new Intent(context, FloatingButtonService.class));
                        }
                    } else {
                        if (statusOverLay) {
                            context.startService(new Intent(context, FloatingButtonService.class));
                        }
                        if (statusShake) {
                            context.startService(new Intent(context, ShakeToTakeScreenShotService.class));
                        } else {
                            context.startService(new Intent(context, CreateNotificationService.class));
                        }
                    }

                    ToolsStatusHelper.saveStatus(true, sharedPreferencesPower);
                }
            }
        }
    }
}
