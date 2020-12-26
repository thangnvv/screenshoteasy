package com.example.screenshoteasy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.screenshoteasy.R;
import com.example.screenshoteasy.utils.SaveImageHelper;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CropActivity extends AppCompatActivity {

    private ImageView magic;
    private ImageButton imgButtonBack, imgButtonCut, imgButtonRotate, imgButtonFlipOver;
    private LinearLayout layoutRotateHolder;
    private TextView txtViewRotateHorizontal, txtViewRotateVertical;
    private CropImageView imgCropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        doInitialization();
        setOnClick();
        Toolbar toolbarCrop = findViewById(R.id.toolBarCrop);
        setSupportActionBar(toolbarCrop);

    }

    private void setOnClick() {
        imgButtonBack.setOnClickListener(v -> finish());

        imgButtonRotate.setOnClickListener(v -> imgCropView.rotateImage(90));

        imgButtonFlipOver.setOnClickListener(v -> {
            layoutRotateHolder.setVisibility(View.VISIBLE);
            magic.setVisibility(View.VISIBLE);
        });

        txtViewRotateHorizontal.setOnClickListener(v -> {
            imgCropView.flipImageHorizontally();
            magic.setVisibility(View.GONE);
            layoutRotateHolder.setVisibility(View.GONE);
        });

        txtViewRotateVertical.setOnClickListener(v -> {
            imgCropView.flipImageVertically();
            magic.setVisibility(View.GONE);
            layoutRotateHolder.setVisibility(View.GONE);
        });

        magic.setOnClickListener(v -> {
            magic.setVisibility(View.GONE);
            layoutRotateHolder.setVisibility(View.GONE);
        });

        imgButtonCut.setOnClickListener(v -> {
            Bitmap cropped = imgCropView.getCroppedImage();
            if(cropped == null){
                Toast.makeText(CropActivity.this, "Something's gone wrong!" , Toast.LENGTH_LONG).show();
            }else {
                SaveImageHelper.saveImageToSdCard(cropped, getApplicationContext());
                Intent intentImageCrop = new Intent(CropActivity.this, GalleryActivity.class);
                intentImageCrop.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentImageCrop);
                finish();
            }
        });
    }

    private void doInitialization() {
        imgButtonBack = findViewById(R.id.imageButtonBackArrowCrop);
        imgButtonCut = findViewById(R.id.imageButtonCut);
        imgButtonFlipOver = findViewById(R.id.imageButtonFlipOver);
        imgButtonRotate = findViewById(R.id.imageButtonRotate);
        imgCropView = findViewById(R.id.imageView);
        layoutRotateHolder = findViewById(R.id.layoutRotateHolder);
        txtViewRotateHorizontal = findViewById(R.id.textViewRotateHorizontal);
        txtViewRotateVertical = findViewById(R.id.textViewRotateVertical);
        magic = findViewById(R.id.magic);

        String imageUrl = getIntent().getStringExtra("imagePath");
        if (!imageUrl.equals("")) {
            imgCropView.setImageBitmap(BitmapFactory.decodeFile(imageUrl));
        }
    }

}
