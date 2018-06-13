package com.bong.naverelabshomework.ImageResult;

import android.graphics.Bitmap;

public class ImageResultItem {
    private String mTitle;
    private String mImageLink;
    private Bitmap mImage;
    private String mThumbnailLink;
    private Bitmap mThumbnailImage;

    public ImageResultItem(String title, String link, String thumbnail) {
        mTitle = title;
        mImageLink = link;
        mImage = null;
        mThumbnailLink = thumbnail;
        mThumbnailImage = null;
    }

    public void setImage(Bitmap image) {mImage = image;}
    public void setThumbnailImage(Bitmap image) {mThumbnailImage = image;}

    public String getTitle() {
        return mTitle;
    }
    public String getImageLink() {
        return mImageLink;
    }
    public Bitmap getImage() { return mImage; }
    public String getThumbnailLink() { return mThumbnailLink; }
    public Bitmap getThumbnailImage() { return mThumbnailImage; }
}
