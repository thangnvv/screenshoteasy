package com.example.screenshoteasy.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.screenshoteasy.utils.ToolsStatusHelper;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.activities.TakingScreenShotActivity;

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
            Intent intentRunning = new Intent(CreateNotificationService.this, TakingScreenShotActivity.class);
            bindForegroundNotification("Running", intentRunning);
        }
        return START_STICKY;
    }

    private void bindForegroundNotification(String contentText, Intent intent){
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        String CHANNEL_ID = "Notification to take Screen Shot";
        String CHANNEL_NAME = "Notification Channel";
        String CHANNEL_DESCRIPTION =  "Notification to take Screen Shot";
        createNotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_DESCRIPTION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("ScreenShot Easy")
                .setContentText(contentText)
                .setAutoCancel(false)
                .setContentIntent(resultPendingIntent);

        startForeground(1, builder.build());
    }

    private void createNotificationChannel(String channel_id, String channel_name, String channel_description) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            channel.setDescription(channel_description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
