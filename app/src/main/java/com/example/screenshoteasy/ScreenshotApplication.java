package com.example.screenshoteasy;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.screenshoteasy.services.CreateNotificationService;
import com.example.screenshoteasy.utils.ToolsStatusHelper;

public class ScreenshotApplication extends Application implements LifecycleObserver {

    private static Context appContext;
    public static boolean isInBackground;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public static Context getAppContext() {
        return appContext;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        isInBackground = false;
        if (ToolsStatusHelper.getStatus(getSharedPreferences("NotificationStatus", MODE_PRIVATE))) {
            recreateNotification();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onMoveToBackground() {
        // app moved to background
        isInBackground = true;
        if (ToolsStatusHelper.getStatus(getSharedPreferences("NotificationStatus", MODE_PRIVATE))) {
            recreateNotification();
        }
    }

    private void recreateNotification() {
        if (ToolsStatusHelper.getStatus(getSharedPreferences("PowerStatus", MODE_PRIVATE))) {
            stopService(new Intent(getAppContext(), CreateNotificationService.class));
            startService(new Intent(getAppContext(), CreateNotificationService.class));
        }
    }

}
