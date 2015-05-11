package com.nkanaev.comics.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.nkanaev.comics.R;
import com.nkanaev.comics.managers.NavigationItem;
import com.nkanaev.comics.managers.NavigationItemAdapter;

import java.util.ArrayList;
import java.util.List;


public class MenuLayout extends RelativeLayout implements ListView.OnItemClickListener {
    private ListView topListView;
    private ListView bottomListView;
    private OnMenuItemSelectListener mListener;

    public interface OnMenuItemSelectListener {
        public void onMenuItemSelected(int resStringRef);
    }

    public MenuLayout(Context context) {
        super(context);
    }

    public MenuLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setMenuItems();
    }

    public void setOnMenuItemSelectListener(OnMenuItemSelectListener listener) {
        mListener = listener;
    }

    private void setMenuItems() {
        topListView = (ListView) findViewById(R.id.navigation_listview_top);
        bottomListView = (ListView) findViewById(R.id.navigation_listview_bottom);

        topListView.setAdapter(new NavigationItemAdapter(getContext(), getTopNavigationItems()));
        bottomListView.setAdapter(new NavigationItemAdapter(getContext(), getBottomNavigationItems()));

        topListView.setOnItemClickListener(this);
        bottomListView.setOnItemClickListener(this);
    }

    private ArrayList<NavigationItem> getTopNavigationItems() {
        ArrayList<NavigationItem> x = new ArrayList<NavigationItem>();
        x.add(new NavigationItem(R.string.menu_library, R.drawable.ic_my_library_books_grey600_24dp));
        x.add(new NavigationItem(R.string.menu_browser, R.drawable.ic_folder_grey600_24dp));
        return x;
    }

    private ArrayList<NavigationItem> getBottomNavigationItems() {
        ArrayList<NavigationItem> x = new ArrayList<NavigationItem>();
        x.add(new NavigationItem(R.string.menu_settings, R.drawable.ic_settings_grey600_24dp));
        x.add(new NavigationItem(R.string.menu_about, R.drawable.ic_info_grey600_24dp));
        return x;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            mListener.onMenuItemSelected((int)id);
        }
    }
}
