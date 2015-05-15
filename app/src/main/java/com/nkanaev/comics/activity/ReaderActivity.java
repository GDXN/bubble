package com.nkanaev.comics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import com.nkanaev.comics.R;
import com.nkanaev.comics.fragment.ReaderFragment;


public class ReaderActivity extends ActionBarActivity {
    private ReaderFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_reader);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            String file = extras.getString(ReaderFragment.PARAM_FILE);
            int page = extras.getInt(ReaderFragment.PARAM_PAGE, 1);
            mFragment = ReaderFragment.create(file, page);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame_reader, mFragment)
                    .commit();
        }

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

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(ReaderFragment.RESULT_CURRENT_PAGE, mFragment.getCurrentPage());
        setResult(ReaderFragment.RESULT, intent);

        super.finish();
    }
}
