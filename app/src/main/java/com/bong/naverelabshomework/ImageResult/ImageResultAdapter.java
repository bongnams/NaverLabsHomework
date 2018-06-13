package com.bong.naverelabshomework.ImageResult;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bong.naverelabshomework.R;

import java.util.ArrayList;

public class ImageResultAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<ImageResultItem> mItems;
    private int mLayout;

    public ImageResultAdapter(Context context, int layout, ArrayList<ImageResultItem> items) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = layout;
        mItems = items;
    }

    @Override
    public ImageResultItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(mLayout, parent, false);
        }

        ImageView thumbnail = convertView.findViewById(R.id.resultImageThumbnail);
        Bitmap thumbnailimage = mItems.get(position).getThumbnailImage();
        if(thumbnailimage == null) {
            thumbnail.setImageBitmap(null);
            new LoadImageTask(thumbnail, mItems.get(position), LoadImageTask.THUMBNAILMODE).execute(mItems.get(position).getThumbnailLink());
        } else {
            thumbnail.setImageBitmap(thumbnailimage);
        }

        TextView title = convertView.findViewById(R.id.resultImageTitle);
        title.setText(Html.fromHtml(mItems.get(position).getTitle()));

        return convertView;
    }
}
