package com.example.screenshoteasy.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.screenshoteasy.ScreenshotApplication;
import com.example.screenshoteasy.activities.MainActivity;
import com.example.screenshoteasy.utils.ToolsStatusHelper;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.activities.TakingScreenShotActivity;
import com.example.screenshoteasy.utils.Utilities;

public class CreateNotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences("NotificationStatus", MODE_PRIVATE);
        boolean notificationStatus = ToolsStatusHelper.getStatus(sharedPreferences);
        if (notificationStatus) {
            Intent intentTap = new Intent(CreateNotificationService.this, TakingScreenShotActivity.class);
            intentTap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(ScreenshotApplication.isInBackground){
                intentTap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            bindForegroundNotification("Tap to screen shot", intentTap);
        } else {
            Intent intentRunning = new Intent(CreateNotificationService.this, MainActivity.class);
            intentRunning.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            bindForegroundNotification("Running", intentRunning);
        }
        return START_STICKY;
    }

    private void bindForegroundNotification(String contentText, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(CreateNotificationService.this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Utilities.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("ScreenShot Easy")
                .setContentText(contentText)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancelAll();
    }
}
