package com.nkanaev.comics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import com.nkanaev.comics.R;
import com.nkanaev.comics.fragment.LibraryBrowserFragment;


public class LibraryBrowserActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_librarybrowser);

        Log.d("SimpleActivity", "OnCreate Started");
        Intent intent = getIntent();
        String path = intent.getStringExtra(LibraryBrowserFragment.PARAM_PATH);
        LibraryBrowserFragment fragment = LibraryBrowserFragment.create(path);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame_browser, fragment)
                    .commit();
        }

        getSupportActionBar().setElevation(8);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
