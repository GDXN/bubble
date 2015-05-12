package com.nkanaev.comics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.nkanaev.comics.R;
import com.nkanaev.comics.fragment.BrowserFragment;


public class BrowserActivity extends ActionBarActivity {
    public final static String DIRECTORY = "BROWSER_DIRECTORY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_browser);

        Log.d("SimpleActivity", "OnCreate Started");
        Intent intent = getIntent();
        String path = intent.getStringExtra(DIRECTORY);
        BrowserFragment fragment = BrowserFragment.create(path);

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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
