package com.nkanaev.comics.managers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.nkanaev.comics.R;


public class NavigationItemAdapter extends ArrayAdapter<NavigationItem> {
    public NavigationItemAdapter(Context context, ArrayList<NavigationItem> items) {
        super(context, 0, items);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).titleResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavigationItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.navigation_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.navigation_title);
        ImageView icon = (ImageView) convertView.findViewById(R.id.navigation_icon);
        title.setText(item.titleResource);
        icon.setImageResource(item.imageResource);
        return convertView;
    }
}
