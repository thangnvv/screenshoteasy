package com.example.screenshoteasy.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.screenshoteasy.activities.MainActivity;
import com.example.screenshoteasy.utils.ToolsStatusHelper;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.activities.TakingScreenShotActivity;
import com.example.screenshoteasy.utils.Utilities;

import java.util.Objects;

public class ShakeToTakeScreenShotService extends Service {

    private SensorManager sensorManager;
    private float accel;
    private float accelCurrent;
    private float accelLast;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferencesNotifi = this.getSharedPreferences("NotificationStatus", MODE_PRIVATE);
        boolean isModeNotification = ToolsStatusHelper.getStatus(sharedPreferencesNotifi);
        if(isModeNotification){
            bindForegroundNotification("Tap to screen shot",
                    new Intent(ShakeToTakeScreenShotService.this, TakingScreenShotActivity.class));
        }else{
            bindForegroundNotification("Running",
                    new Intent(ShakeToTakeScreenShotService.this, MainActivity.class));
        }

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accel = 10f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
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

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public synchronized void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt((x * x + y * y + z * z));
            float delta = accelCurrent - accelLast;
            accel = accel * 0.9f + delta;
            if (accel > 12) {
                Intent intent = new Intent(getApplicationContext(), TakingScreenShotActivity.class);
                if(!Utilities.isAppOnForeGround){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }else{
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(mSensorListener);
        stopForeground(true);
    }
}
