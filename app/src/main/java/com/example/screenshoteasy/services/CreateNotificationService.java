package com.example.screenshoteasy.services;

import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
        SharedPreferences sharedPreferences =  getSharedPreferences("NotificationStatus", MODE_PRIVATE);
        boolean notificationStatus = ToolsStatusHelper.getStatus(sharedPreferences);
        if(notificationStatus){
            Intent intentTap = new Intent(CreateNotificationService.this, TakingScreenShotActivity.class);
            bindForegroundNotification("Tap to screen shot", intentTap);
        }else{
            Intent intentRunning = new Intent(CreateNotificationService.this, MainActivity.class);
            bindForegroundNotification("Running", intentRunning);
        }
        return START_STICKY;
    }

    private void bindForegroundNotification(String contentText, Intent intent){
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Utilities.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("ScreenShot Easy")
                .setContentText(contentText)
                .setAutoCancel(false)
                .setContentIntent(resultPendingIntent);

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
