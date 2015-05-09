package com.nkanaev.comics.activity;

import android.content.res.TypedArray;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import com.nkanaev.comics.fragment.GroupBrowserFragment;
import com.nkanaev.comics.R;
import com.nkanaev.comics.managers.LocalCoverHandler;
import com.nkanaev.comics.managers.Scanner;

import com.nkanaev.comics.managers.Utils;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;


public class MainActivity extends ActionBarActivity {
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
            GroupBrowserFragment groupBrowserFragment = new GroupBrowserFragment();
            pushFragment(groupBrowserFragment, false);
        }

        getSupportActionBar().setElevation(8);
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

    public void pushFragment(Fragment fragment, boolean allow_back) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_layout, fragment);
        if (allow_back) {
            transaction = transaction.addToBackStack(((Object)fragment).getClass().getSimpleName());
        }
        transaction.commit();
    }

    public void popLastFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        if (manager.getBackStackEntryCount() == 1) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        popLastFragment();
    }

    @Override
    public boolean onSupportNavigateUp() {
        popLastFragment();
        return super.onSupportNavigateUp();
    }
}
