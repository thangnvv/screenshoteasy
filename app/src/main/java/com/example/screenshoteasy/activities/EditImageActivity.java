package com.example.screenshoteasy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.screenshoteasy.adapters.ColorPickerAdapter;
import com.example.screenshoteasy.R;
import com.example.screenshoteasy.utils.SaveImageHelper;
import com.example.screenshoteasy.utils.Utilities;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class EditImageActivity extends AppCompatActivity {

    private ImageButton imgButtonEditImageAddText, imgButtonEditImageBrush, imgButtonEditImageBrushColor, imgButtonBackArrowEdit,
            imgButtonUndo, imgButtonSave, imgButtonDelete;
    private PhotoEditor photoEditor;
    private int mColorCode = R.color.black;
    private int brushSize = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        Toolbar toolbar = findViewById(R.id.toolBarEditImage);
        setSupportActionBar(toolbar);
        doInitialization();
        setOnClick();
    }

    private void doInitialization() {
        imgButtonBackArrowEdit = findViewById(R.id.imageButtonBackArrowEdit);
        imgButtonEditImageAddText = findViewById(R.id.buttonEditImageAddText);
        imgButtonEditImageBrush = findViewById(R.id.buttonEditImageBrush);
        imgButtonEditImageBrushColor = findViewById(R.id.buttonEditImageChooseBrushColor);
        imgButtonDelete = findViewById(R.id.imageButtonDelete);
        imgButtonUndo = findViewById(R.id.imageButtonUndo);
        imgButtonSave = findViewById(R.id.imageButtonSave);
        PhotoEditorView photoEditorView = findViewById(R.id.photoEditorView);

        String imageUrl = getIntent().getStringExtra("imagePath");
        if (!imageUrl.equals("")) {
            photoEditorView.getSource().setImageBitmap(BitmapFactory.decodeFile(imageUrl));

            //Use custom font using latest support library
            Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto);

            photoEditor = new PhotoEditor.Builder(this, photoEditorView)
                    .setPinchTextScalable(true)
                    .setDefaultTextTypeface(mTextRobotoTf)
                    .build();
        }
    }

    private void setOnClick() {

        imgButtonEditImageAddText.setOnClickListener(v -> {
            final Dialog dialogAddText = new Dialog(EditImageActivity.this);
            dialogAddText.setContentView(R.layout.dialog_add_text);

            MaterialButton buttonDone = dialogAddText.findViewById(R.id.buttonDone);
            final EditText edtAddText = dialogAddText.findViewById(R.id.editText);
            RecyclerView recyclerColors = dialogAddText.findViewById(R.id.recyclerColors);
            recyclerColors.setHasFixedSize(true);

            ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(EditImageActivity.this);
            //This listener will change the text color when clicked on any color from picker
            colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
                mColorCode = colorCode;
                edtAddText.setTextColor(colorCode);
            });
            recyclerColors.setAdapter(colorPickerAdapter);

            buttonDone.setOnClickListener(v1 -> {
                if (edtAddText.getText().toString().trim().length() != 0) {
                    photoEditor.addText(String.valueOf(edtAddText.getText()), mColorCode);
                    dialogAddText.dismiss();
                    showToolButton();
                } else {
                    Toast.makeText(EditImageActivity.this, "Blank", Toast.LENGTH_SHORT).show();
                    dialogAddText.cancel();
                }
            });
            dialogAddText.show();
        });

        imgButtonEditImageBrush.setOnClickListener(v -> {

            final Dialog dialogSettingBrush = new Dialog(EditImageActivity.this);
            dialogSettingBrush.setContentView(R.layout.dialog_setting_brush);
            dialogSettingBrush.show();

            final TextView txtViewShowBrushSize = dialogSettingBrush.findViewById(R.id.textViewShowBrushSize);
            SeekBar skBrushSize = dialogSettingBrush.findViewById(R.id.seekbarBrushSize);
            Button btnSetBrushSize = dialogSettingBrush.findViewById(R.id.buttonSetBrushSize);

            skBrushSize.setProgress(brushSize);
            txtViewShowBrushSize.setText((brushSize + ""));

            skBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    txtViewShowBrushSize.setText((progress + ""));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    brushSize = seekBar.getProgress();
                }
            });

            btnSetBrushSize.setOnClickListener(v14 -> {
                photoEditor.setBrushDrawingMode(true);
                photoEditor.setBrushSize(brushSize);
                dialogSettingBrush.dismiss();
                showToolButton();
            });

            photoEditor.setBrushColor(getResources().getColor(R.color.colorPrimary, null));
        });

        imgButtonEditImageBrushColor.setOnClickListener(v -> {

            final Dialog dialogPickBrushColor = new Dialog(EditImageActivity.this);
            dialogPickBrushColor.setContentView(R.layout.dialog_pick_color);
            RecyclerView recyclerView = dialogPickBrushColor.findViewById(R.id.recyclerViewColor);
            ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(EditImageActivity.this);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(EditImageActivity.this, 4, GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(colorPickerAdapter);
            recyclerView.setHasFixedSize(true);
            colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
                photoEditor.setBrushColor(colorCode);
                dialogPickBrushColor.dismiss();
                showToolButton();
            });
            dialogPickBrushColor.show();
        });

        imgButtonBackArrowEdit.setOnClickListener(v -> finish());

        imgButtonSave.setOnClickListener(v -> photoEditor.saveAsBitmap(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(@NonNull Bitmap saveBitmap) {
                SaveImageHelper.saveImageToSdCard(saveBitmap, getApplicationContext());
                Intent intentImageEdit = new Intent(EditImageActivity.this, GalleryActivity.class);
                intentImageEdit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentImageEdit);
                finish();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(EditImageActivity.this, "Something's gone wrong!", Toast.LENGTH_LONG).show();
            }
        }));

        imgButtonUndo.setOnClickListener(v -> photoEditor.undo());

        imgButtonDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(EditImageActivity.this)
                    .setTitle(getResources().getString(R.string.are_you_sure))
                    .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> finish())
                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                    .show();
        });

    }

    private void showToolButton() {
        imgButtonDelete.setVisibility(View.VISIBLE);
        imgButtonUndo.setVisibility(View.VISIBLE);
        imgButtonSave.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utilities.isAppOnForeGround = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.isAppOnForeGround = true;
    }

}