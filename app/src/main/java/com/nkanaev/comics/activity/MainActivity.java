package com.nkanaev.comics.activity;

import android.content.res.Configuration;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import com.nkanaev.comics.fragment.DirectoryBrowserFragment;
import com.nkanaev.comics.fragment.LibraryGroupBrowserFragment;
import com.nkanaev.comics.R;
import com.nkanaev.comics.managers.*;

import com.nkanaev.comics.view.MenuLayout;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;


public class MainActivity extends ActionBarActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Scanner mScanner = null;
    private OnRefreshListener mRefreshListener;
    private Picasso mPicasso;

    public interface OnRefreshListener {
        void onRefreshStart();
        void onRefreshEnd();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mPicasso == null) {
            mPicasso = new Picasso.Builder(this)
                    .addRequestHandler(new LocalCoverHandler(this))
                    .memoryCache(new LruCache(Utils.calculateMemorySize(this, 10)))
                    .build();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        if (savedInstanceState == null) {
            LibraryGroupBrowserFragment groupBrowserFragment = new LibraryGroupBrowserFragment();
            pushFragment(groupBrowserFragment, false);
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

    public Picasso getPicasso() {
        return mPicasso;
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }

    private void refreshLibrary() {
        if (mScanner == null || mScanner.getStatus() == AsyncTask.Status.FINISHED) {

            mScanner = new Scanner(this) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mRefreshListener.onRefreshStart();
                }

                @Override
                protected void onPostExecute(Long aLong) {
                    super.onPostExecute(aLong);
                    mRefreshListener.onRefreshEnd();
                }
            };
            mScanner.execute(Environment.getExternalStorageDirectory());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshLibrary();
                break;
            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void pushFragment(Fragment fragment, boolean allow_back) {
//        FragmentTransaction transaction = getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.content_frame, fragment);
//        if (allow_back) {
//            transaction = transaction.addToBackStack(((Object)fragment).getClass().getSimpleName());
//        }
//        transaction.commit();
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
