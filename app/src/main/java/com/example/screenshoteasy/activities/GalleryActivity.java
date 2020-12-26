package com.example.screenshoteasy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.screenshoteasy.adapters.PictureAdapter;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.utils.Utilities;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class GalleryActivity extends AppCompatActivity {
    private PictureAdapter pictureAdapter;
    private RecyclerView recyclerPicture;
    private List<String> listFiles = new ArrayList<>();
    private File[] filesArray;
    private ImageButton imgButtonBackArrow, imgButtonCrop, imgButtonShare, imgButtonDelete, imgButtonEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getFilesFromSdcard();
        doInitialization();
        setOnClick();
    }

    public void getFilesFromSdcard() {
        File file = new File(android.os.Environment.getExternalStorageDirectory(), "/ScreenShotEasy");
        if (file.isDirectory()) {
            filesArray = file.listFiles();
            if (filesArray != null && filesArray.length > 0) {
                for (int i = 0; i < filesArray.length - 1; i++){
                    File temp;
                    for(int j = i + 1; j < filesArray.length; j++){
                       if(filesArray[i].lastModified() <= filesArray[j].lastModified()){
                           temp = filesArray[i];
                           filesArray[i] = filesArray[j];
                           filesArray[j] = temp;
                       }
                   }
                }

                for(File fileName : filesArray){
                    listFiles.add(fileName.getAbsolutePath());
                }
            }
        }
    }

        private void setOnClick () {

            imgButtonBackArrow.setOnClickListener(v -> onBackPressed());
            imgButtonCrop.setOnClickListener(v -> {
                if (listFiles.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Gallery is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    int position = ((LinearLayoutManager) Objects.requireNonNull(recyclerPicture.getLayoutManager())).findLastCompletelyVisibleItemPosition();
                    Intent intentCropImage = new Intent(GalleryActivity.this, CropActivity.class);
                    intentCropImage.putExtra("imagePath", listFiles.get(position));
                    startActivity(intentCropImage);
                }
            });

            imgButtonDelete.setOnClickListener(v -> {
                if (listFiles.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Gallery is empty!", Toast.LENGTH_SHORT).show();
                } else {

                    new MaterialAlertDialogBuilder(GalleryActivity.this)
                            .setTitle(getResources().getString(R.string.are_you_sure))
                            .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                                int position = ((LinearLayoutManager) Objects.requireNonNull(recyclerPicture.getLayoutManager())).findLastCompletelyVisibleItemPosition();
                                if (!new File(listFiles.get(position)).delete()) {
                                    Toast.makeText(getApplicationContext(), "Delete image failed!", Toast.LENGTH_SHORT).show();
                                }
                                listFiles.remove(position);
                                pictureAdapter.notifyItemRemoved(position);
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                }
            });

            imgButtonShare.setOnClickListener(v -> {
                if (listFiles.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Gallery is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    int position = ((LinearLayoutManager) Objects.requireNonNull(recyclerPicture.getLayoutManager())).findLastCompletelyVisibleItemPosition();
                    Intent intentShareImage = new Intent(Intent.ACTION_SEND);
                    intentShareImage.setType("image/jpg");
                    Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), this.getApplicationContext().getPackageName() + ".provider", new File(listFiles.get(position)));
                    intentShareImage.putExtra(Intent.EXTRA_STREAM, photoURI);
                    intentShareImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intentShareImage, "Share Image Via"));
                }
            });

            imgButtonEdit.setOnClickListener(v -> {
                if (listFiles.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Gallery is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    int position = ((LinearLayoutManager) Objects.requireNonNull(recyclerPicture.getLayoutManager())).findLastCompletelyVisibleItemPosition();
                    Intent intentImageEdit = new Intent(GalleryActivity.this, EditImageActivity.class);
                    intentImageEdit.putExtra("imagePath", listFiles.get(position));
                    startActivity(intentImageEdit);
                }
            });

        }

        private void doInitialization () {
            imgButtonBackArrow = findViewById(R.id.imageButtonBackArrowGallery);
            imgButtonCrop = findViewById(R.id.imageButtonCrop);
            imgButtonShare = findViewById(R.id.imageButtonShare);
            imgButtonDelete = findViewById(R.id.imageButtonDelete);
            imgButtonEdit = findViewById(R.id.imageButtonEdit);
            recyclerPicture = findViewById(R.id.recyclerview);

            pictureAdapter = new PictureAdapter(this, listFiles);
            SnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(recyclerPicture);
            recyclerPicture.setAdapter(pictureAdapter);
        }

        @Override
        public void onBackPressed () {
            super.onBackPressed();
            startActivity(new Intent(GalleryActivity.this, MainActivity.class));
            finish();
        }

        @Override
        protected void onPause () {
            super.onPause();
            Utilities.isAppOnForeGround = false;
        }

        @Override
        protected void onResume () {
            super.onResume();
            Utilities.isAppOnForeGround = true;
        }
    }
