package com.example.screenshoteasy.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.screenshoteasy.R;
import com.gjiazhe.scrollparallaximageview.ScrollParallaxImageView;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureViewHolder> {
    private LayoutInflater mInflater;
    private List<String> listFiles;

    public PictureAdapter(Context context, List<String> listFiles) {
        mInflater = LayoutInflater.from(context);
        this.listFiles = listFiles;
    }

    @NonNull
    @Override
    public PictureAdapter.PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mPictureView = mInflater.inflate(R.layout.item_container_image, parent, false);
        return new PictureViewHolder(mPictureView);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureAdapter.PictureViewHolder holder, int position) {
        Bitmap mCurrent = BitmapFactory.decodeFile(listFiles.get(position));
        holder.imgPicture.setImageBitmap(mCurrent);
        holder.imgPicture.setParallaxStyles(new MyParallaxStyle());
    }

    @Override
    public int getItemCount() {
        return listFiles.size();
    }


    class PictureViewHolder extends RecyclerView.ViewHolder {

        ScrollParallaxImageView imgPicture;

        public PictureViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPicture = itemView.findViewById(R.id.imageViewPicture);
        }
    }

    public class MyParallaxStyle implements ScrollParallaxImageView.ParallaxStyle {

        @Override
        public void onAttachedToImageView(ScrollParallaxImageView view) {

        }

        @Override
        public void onDetachedFromImageView(ScrollParallaxImageView view) {
        }

        @Override
        public void transform(ScrollParallaxImageView view, Canvas canvas, int x, int y) {
            float finalScaleRatio = 0.8f;

            int vWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
            int vHeight = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();

            // device's width
            int dWidth = view.getResources().getDisplayMetrics().widthPixels;
            if (vWidth >= dWidth) {
                // Do nothing if imageView's width is bigger than device's width.
                return;
            }

            float scale;
//            pivot is the free space of one side in the recyclerview

            int pivot = (dWidth - vWidth) / 2;
            if (x <= pivot) {
                scale = 2 * (1 - finalScaleRatio) * (x + vWidth) / (dWidth + vWidth) + finalScaleRatio;
            } else {
                scale = 2 * (1 - finalScaleRatio) * (dWidth - x) / (dWidth + vWidth) + finalScaleRatio;
            }

            // original is vHeight/2 and it scales top and bottom
            canvas.scale(scale, scale, vWidth / 2, vHeight);
        }
    }
}
