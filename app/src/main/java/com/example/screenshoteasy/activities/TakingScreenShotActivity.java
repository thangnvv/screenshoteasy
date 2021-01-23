package com.example.screenshoteasy.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.widget.Toast;

import com.example.screenshoteasy.ScreenshotApplication;
import com.example.screenshoteasy.utils.ToolsStatusHelper;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.utils.SaveImageHelper;
import com.example.screenshoteasy.services.FloatingButtonService;

import java.nio.ByteBuffer;

public class TakingScreenShotActivity extends Activity {

    private static final int REQUEST_CODE = 100;
    private static final String SCREENSHOT_NAME = "Screenshot Application";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private TakingScreenShotActivity.OrientationChangeCallback mOrientationChangeCallback;
    private static MediaProjection sMediaProjection;
    private static MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler = new Handler();
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private boolean wasCapture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taking_screenshot);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        temporaryHideFloatingButton();
        new Handler(Looper.getMainLooper()).postDelayed(this::startProjection, 1000);
    }

    private void temporaryHideFloatingButton() {
        if(ToolsStatusHelper.getStatus(getSharedPreferences("OverlayStatus", MODE_PRIVATE))){
            stopService(new Intent(this, FloatingButtonService.class));
        }
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            Bitmap bitmap;
            try {
                if (!wasCapture) {
                    wasCapture = true;
                    image = reader.acquireLatestImage();
                    sMediaProjection.stop();
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * mWidth;
                        // create bitmap
                        bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        Rect rect = image.getCropRect();
                        Bitmap croppedBitmap =  Bitmap.createBitmap(bitmap,rect.left,rect.top,rect.width(),rect.height());
                        SaveImageHelper.saveImageToSdCard(croppedBitmap, TakingScreenShotActivity.this);
                        restartService();
                        // Hide those if mode save silently is on
                        if(!ToolsStatusHelper.getStatus(getSharedPreferences("sharedPreferencesSaveSilently", MODE_PRIVATE))){
                            Intent intentSendImageToGallery = new Intent(TakingScreenShotActivity.this, GalleryActivity.class);
                            intentSendImageToGallery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intentSendImageToGallery);
                        }else{
                            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }

        }
    }

    private void restartService(){
        if (ToolsStatusHelper.getStatus(getSharedPreferences("OverlayStatus", MODE_PRIVATE))) {
            startService(new Intent(TakingScreenShotActivity.this, FloatingButtonService.class));
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(() -> {
                if (mVirtualDisplay != null) mVirtualDisplay.release();
                if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
            });
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    public void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    /****************************************** Factoring Virtual Display creation ****************/
    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createVirtualDisplay() {
        // get width and height
        mDensity = getResources().getDisplayMetrics().densityDpi;
        mDisplay =  TakingScreenShotActivity.this.getDisplay();
        Point windowSize = new Point();
        getApplicationContext().getDisplay().getRealSize(windowSize);
        mWidth = windowSize.x;
        mHeight = windowSize.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENSHOT_NAME, mWidth, mHeight,
                mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new TakingScreenShotActivity.ImageAvailableListener(), mHandler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection != null) {
                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new TakingScreenShotActivity.OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }
                // register media projection stop callback
                sMediaProjection.registerCallback(new TakingScreenShotActivity.MediaProjectionStopCallback(), mHandler);
            }
        }
    }

}