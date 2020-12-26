package com.example.screenshoteasy.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.screenshoteasy.services.CreateNotificationService;
import com.example.screenshoteasy.services.FloatingButtonService;
import com.example.screenshoteasy.services.ShakeToTakeScreenShotService;
import com.example.screenshoteasy.utils.ToolsStatusHelper;

import static android.content.Context.MODE_PRIVATE;

public class BroadCastStopService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOffListener.onScreenOff();
            ToolsStatusHelper.saveStatus(false, context.getSharedPreferences("PowerStatus", MODE_PRIVATE));
            if (ToolsStatusHelper.getStatus(context.getSharedPreferences("OverlayStatus", MODE_PRIVATE))) {
                context.stopService(new Intent(context, FloatingButtonService.class));
            }
            if (ToolsStatusHelper.getStatus(context.getSharedPreferences("ShakeStatus", MODE_PRIVATE))) {
                context.stopService(new Intent(context, ShakeToTakeScreenShotService.class));
            }
            context.stopService(new Intent(context, CreateNotificationService.class));
        }
    }

    private ScreenOffListener screenOffListener;

    public void setScreenOffListener(ScreenOffListener screenOffListener) {
        this.screenOffListener = screenOffListener;
    }

    public interface ScreenOffListener {
        void onScreenOff();
    }
}
