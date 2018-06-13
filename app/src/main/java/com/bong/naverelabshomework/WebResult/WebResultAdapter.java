package com.bong.naverelabshomework.WebResult;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bong.naverelabshomework.R;

import java.util.ArrayList;

public class WebResultAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<WebResultItem> mItems;
    private int mLayout;

    public WebResultAdapter(Context context, int layout, ArrayList<WebResultItem> items) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = layout;
        mItems = items;
    }

    @Override
    public WebResultItem getItem(int position) {
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

        TextView title = (TextView) convertView.findViewById(R.id.resultWebTitle);
        title.setText(Html.fromHtml(mItems.get(position).getTitle()));

        TextView description = (TextView) convertView.findViewById(R.id.resultWebDes);
        description.setText(Html.fromHtml(mItems.get(position).getDescription()));

        TextView link = (TextView) convertView.findViewById(R.id.resultWebLink);
        link.setText(Html.fromHtml(mItems.get(position).getLink()));

        return convertView;
    }
}
