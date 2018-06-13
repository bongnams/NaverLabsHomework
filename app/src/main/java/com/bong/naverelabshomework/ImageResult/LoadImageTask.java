package com.bong.naverelabshomework.ImageResult;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
    public static final int IMAGEMODE = 0;
    public static final int THUMBNAILMODE = 1;

    private ImageView mImageView = null;
    private ImageResultItem mItem;
    private int mMode;

    public LoadImageTask(ImageView imageview, ImageResultItem item, int mode) {
        mImageView = imageview;
        mItem = item;
        mMode = mode;
    }

    protected Bitmap doInBackground(String... urls) {
        Bitmap image = null;
        try {
            InputStream is = new URL(urls[0]).openStream();
            image = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        mImageView.setImageBitmap(result);
        if(mMode == IMAGEMODE) {
            mItem.setImage(result);
        } else {
            mItem.setThumbnailImage(result);
        }
    }
}
