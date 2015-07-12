package com.nkanaev.comics.activity;

import android.content.res.Configuration;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import com.nkanaev.comics.fragment.BrowserFragment;
import com.nkanaev.comics.fragment.LibraryFragment;
import com.nkanaev.comics.R;

import com.nkanaev.comics.view.AboutDialog;
import com.nkanaev.comics.view.MenuLayout;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends ActionBarActivity
        implements MenuLayout.OnMenuItemSelectListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private BrowserFragment mBrowserFragment;
    private LibraryFragment mLibraryFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.layout_main);

        if (savedInstanceState == null) {
            LibraryFragment groupBrowserFragment = new LibraryFragment();
            setFragment(groupBrowserFragment);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        MenuLayout menuLayout = (MenuLayout) findViewById(R.id.navigation_layout);
        menuLayout.setOnMenuItemSelectListener(this);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mBrowserFragment = new BrowserFragment();
        mLibraryFragment = new LibraryFragment();

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

    @Override
    public void onMenuItemSelected(int resStringRef) {
        switch (resStringRef) {
            case R.string.menu_browser:
                setFragment(mBrowserFragment);
                break;
            case R.string.menu_library:
                setFragment(mLibraryFragment);
                break;
            case R.string.menu_about:
                new AboutDialog(this).show();
                break;
        }
        mDrawerLayout.closeDrawers();
    }
}
