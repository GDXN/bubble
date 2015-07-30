package com.nkanaev.comics.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v7.widget.Toolbar;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.aboutlibraries.ui.LibsFragment;

import com.nkanaev.comics.fragment.BrowserFragment;
import com.nkanaev.comics.fragment.LibraryFragment;
import com.nkanaev.comics.R;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private BrowserFragment mBrowserFragment;
    private LibraryFragment mLibraryFragment;
    private LibsFragment mAboutFragment;
    private MenuItem mPreviousMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(8);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupNavigationView(navigationView);
        mPreviousMenuItem = navigationView.getMenu().findItem(R.id.drawer_menu_library);

        if (savedInstanceState == null) {
            LibraryFragment groupBrowserFragment = new LibraryFragment();
            setFragment(groupBrowserFragment);
        }
        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 1) {
                mDrawerToggle.setDrawerIndicatorEnabled(false);
            }
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        mBrowserFragment = new BrowserFragment();
        mLibraryFragment = new LibraryFragment();
        mAboutFragment = createAboutFragment();
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

    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();

        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    private boolean popFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            if (fragmentManager.getBackStackEntryCount() == 1) {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            }

            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    private void setupNavigationView(NavigationView view) {
        view.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.drawer_menu_library:
                        setFragment(mLibraryFragment);
                        break;
                    case R.id.drawer_menu_browser:
                        setFragment(mBrowserFragment);
                        break;
                    case R.id.drawer_menu_about:
                        setFragment(mAboutFragment);
                        break;
                }
                if (mPreviousMenuItem != null) {
                    mPreviousMenuItem.setChecked(false);
                }
                menuItem.setChecked(true);
                mPreviousMenuItem = menuItem;
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private LibsFragment createAboutFragment() {
        final List<String> libraryOrder = Arrays.asList("Picasso", "Junrar", "AboutLibraries");

        return new LibsBuilder()
                .withAboutIconShown(true)
                .withAboutAppName(getString(R.string.app_name))
                .withAboutDescription(getString(R.string.app_description))
                .withAboutVersionShown(true)
                .withAnimations(false)
                .withLicenseShown(true)
                .withFields(R.string.class.getFields())
                .withLibraries("Picasso", "Junrar")
                .withLibraryComparator(new Comparator<Library>() {
                    @Override
                    public int compare(Library l1, Library l2) {
                        Integer i1 = libraryOrder.indexOf(l1.getDefinedName());
                        Integer i2 = libraryOrder.indexOf(l2.getDefinedName());
                        return i1.compareTo(i2);
                    }
                })
                .fragment();
    }

    @Override
    public void onBackPressed() {
        if (!popFragment()) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!popFragment()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawers();
            else
                mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onSupportNavigateUp();
    }
}
