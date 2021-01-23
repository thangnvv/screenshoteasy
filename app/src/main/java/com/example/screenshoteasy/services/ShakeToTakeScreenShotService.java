package com.example.screenshoteasy.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;

import com.example.screenshoteasy.ScreenshotApplication;
import com.example.screenshoteasy.activities.TakingScreenShotActivity;
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

    @Override
    public void onCreate() {
        super.onCreate();
        bindForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accel = 10f;
        accelCurrent = SensorManager.GRAVITY_EARTH; 
        accelLast = SensorManager.GRAVITY_EARTH;
        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    private void bindForegroundService(){
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        if(notifications.length > 0){
            Notification notification = notifications[0].getNotification();
            notificationManager.cancelAll();
            startForeground(1, notification);
        }
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
                Intent intentTakeScreenShot = new Intent(ShakeToTakeScreenShotService.this, TakingScreenShotActivity.class);
                intentTakeScreenShot.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(ScreenshotApplication.isInBackground){
                    intentTakeScreenShot.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                startActivity(intentTakeScreenShot);
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
        stopForeground(false);
    }
}
