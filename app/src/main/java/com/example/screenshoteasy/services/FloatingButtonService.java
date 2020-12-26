package com.example.screenshoteasy.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.GestureDetector;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.screenshoteasy.R;
import com.example.screenshoteasy.activities.TakingScreenShotActivity;
import com.example.screenshoteasy.utils.Utilities;

public class FloatingButtonService extends Service {
    private GestureDetector gestureDetector;
    private WindowManager windowManager;
    private ImageView takeScreenShot;
    private boolean activity_background;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            activity_background = intent.getBooleanExtra("activity_background", false);
        }
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        takeScreenShot = new ImageView(this);
        takeScreenShot.setImageResource(R.drawable.camera_button);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        windowManager.addView(takeScreenShot, params);
        try {

            takeScreenShot.setOnTouchListener(new View.OnTouchListener() {

                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent intent = new Intent(FloatingButtonService.this, TakingScreenShotActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if(!Utilities.isAppOnForeGround){
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                    if (gestureDetector.onTouchEvent(event)) {
                        startActivity(intent);
                        stopSelf();
                        return true;
                    } else {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = paramsF.x;
                                initialY = paramsF.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_UP:
                                if (activity_background) {
                                    float xDiff = event.getRawX() - initialTouchX;
                                    float yDiff = event.getRawY() - initialTouchY;

                                    if ((Math.abs(xDiff) < 5) && (Math.abs(yDiff) < 5)) {
                                        startActivity(intent);
                                        stopSelf();
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_MOVE:
                                paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                                paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                                windowManager.updateViewLayout(takeScreenShot, paramsF);
                                break;
                        }
                        return false;
                    }
                }
            });

        } catch (Exception e) {
            // TODO: handle exception
        }
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(takeScreenShot);
    }

    private static class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

}
