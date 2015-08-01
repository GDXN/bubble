package com.nkanaev.comics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.nkanaev.comics.R;
import com.nkanaev.comics.fragment.ReaderFragment;

import java.io.File;


public class ReaderActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_reader);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reader);
        setSupportActionBar(toolbar);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        toolbar.setLayoutParams(params);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            ReaderFragment fragment = null;
            ReaderFragment.Mode mode = (ReaderFragment.Mode) extras.getSerializable(ReaderFragment.PARAM_MODE);
            if (mode == ReaderFragment.Mode.MODE_LIBRARY) {
                fragment = ReaderFragment.create(extras.getInt(ReaderFragment.PARAM_HANDLER));
            }
            else {
                fragment = ReaderFragment.create((File) extras.getSerializable(ReaderFragment.PARAM_HANDLER));
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame_reader, fragment)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
