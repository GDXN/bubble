package com.nkanaev.comics.activity;

import android.content.res.Configuration;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import com.nkanaev.comics.fragment.DirectoryBrowserFragment;
import com.nkanaev.comics.fragment.LibraryGroupBrowserFragment;
import com.nkanaev.comics.R;

import com.nkanaev.comics.view.MenuLayout;


public class MainActivity extends ActionBarActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        if (savedInstanceState == null) {
            LibraryGroupBrowserFragment groupBrowserFragment = new LibraryGroupBrowserFragment();
            setFragment(groupBrowserFragment);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        MenuLayout menuLayout = (MenuLayout) findViewById(R.id.navigation_layout);
        menuLayout.setOnMenuItemSelectListener(new MenuLayout.OnMenuItemSelectListener() {
            @Override
            public void onMenuItemSelected(int resStringRef) {
                if (resStringRef == R.string.menu_browser) {
                    setFragment(new DirectoryBrowserFragment());
                }
                else if (resStringRef == R.string.menu_library) {
                    setFragment(new LibraryGroupBrowserFragment());
                }
                mDrawerLayout.closeDrawers();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        getSupportActionBar().setElevation(8);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        }
        else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onSupportNavigateUp();
    }
}
