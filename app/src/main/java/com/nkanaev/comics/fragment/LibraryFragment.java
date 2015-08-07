package com.nkanaev.comics.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;

import com.nkanaev.comics.Constants;
import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.MainActivity;
import com.nkanaev.comics.managers.LocalCoverHandler;
import com.nkanaev.comics.managers.Scanner;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.view.DirectorySelectDialog;
import com.squareup.picasso.Picasso;


public class LibraryFragment extends Fragment
        implements
        DirectorySelectDialog.OnDirectorySelectListener,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {
    private final static String BUNDLE_DIRECTORY_DIALOG_SHOWN = "BUNDLE_DIRECTORY_DIALOG_SHOWN";

    private ArrayList<Comic> mComics;
    private DirectorySelectDialog mDirectorySelectDialog;
    private SwipeRefreshLayout mRefreshLayout;
    private GridView mGridView;
    private Storage mStorage;
    private Scanner mScanner;
    private Picasso mPicasso;

    private boolean mFirstLaunch = false;

    public LibraryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorage = Storage.getStorage(getActivity());
        getComics();

        if (mComics.size() == 0) {
            SharedPreferences preferences = getActivity()
                    .getSharedPreferences(Constants.SETTINGS_NAME, 0);
            mFirstLaunch = !preferences.contains(Constants.SETTINGS_LIBRARY_DIR);
        }

        mDirectorySelectDialog = new DirectorySelectDialog(getActivity());
        mDirectorySelectDialog.setCurrentDirectory(Environment.getExternalStorageDirectory());
        mDirectorySelectDialog.setOnDirectorySelectListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_library, container, false);

        mPicasso = ((MainActivity) getActivity()).getPicasso();

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragmentLibraryLayout);
        mRefreshLayout.setColorSchemeColors(R.color.primary);
        mRefreshLayout.setEnabled(!mFirstLaunch);
        mRefreshLayout.setOnRefreshListener(this);

        mGridView = (GridView) view.findViewById(R.id.groupGridView);
        mGridView.setAdapter(new GroupBrowserAdapter());
        mGridView.setOnItemClickListener(this);

        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_group_column_width);
        int numColumns = Math.round((float) deviceWidth / columnWidth);
        mGridView.setNumColumns(numColumns);

        if (mFirstLaunch) {
            mDirectorySelectDialog.show();
        }

        getActivity().setTitle(R.string.menu_library);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.library, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuLibrarySetDir) {
            if (mScanner != null && mScanner.getStatus() != AsyncTask.Status.FINISHED)
                mScanner.cancel(true);

            mDirectorySelectDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_DIRECTORY_DIALOG_SHOWN,
                (mDirectorySelectDialog != null) && mDirectorySelectDialog.isShowing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDirectorySelect(File file) {
        mFirstLaunch = false;
        mRefreshLayout.setEnabled(true);

        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constants.SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.SETTINGS_LIBRARY_DIR, file.getAbsolutePath());
        editor.apply();

        if (mScanner == null || mScanner.getStatus() == AsyncTask.Status.FINISHED) {
            mScanner = new Scanner(getActivity(), mStorage, file) {
                @Override
                protected void onPreExecute() {
                    mRefreshLayout.setRefreshing(true);
                    mComics.clear();
                    mGridView.requestLayout();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mRefreshLayout.setRefreshing(false);
                    mRefreshLayout.setEnabled(true);
                    getComics();
                    mGridView.requestLayout();
                }
            };
            mScanner.execute();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Comic comic = mComics.get(position);

        String path = comic.getFile().getParent();
        LibraryBrowserFragment fragment = LibraryBrowserFragment.create(path);
        ((MainActivity)getActivity()).pushFragment(fragment);
    }

    @Override
    public void onRefresh() {
        if (mScanner == null || mScanner.getStatus() == AsyncTask.Status.FINISHED) {
            String libraryDir = getActivity()
                    .getSharedPreferences(Constants.SETTINGS_NAME, 0)
                    .getString(Constants.SETTINGS_LIBRARY_DIR, null);
            if (libraryDir == null)
                return;

            mScanner = new Scanner(getActivity(), mStorage, new File(libraryDir)) {
                @Override
                protected void onPreExecute() {
                    mRefreshLayout.setRefreshing(true);
                    mComics.clear();
                    mGridView.requestLayout();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mRefreshLayout.setRefreshing(false);
                    getComics();
                    mGridView.requestLayout();
                }
            };
            mScanner.execute();
        }
    }

    private void getComics() {
        mComics = Storage.getStorage(getActivity()).listDirectoryComics();
        Collections.sort(mComics, new Comparator<Comic>() {
            @Override
            public int compare(Comic lhs, Comic rhs) {
                return lhs.getFile().getParentFile().getName()
                        .compareTo(rhs.getFile().getParentFile().getName());
            }
        });
    }

    private final class GroupBrowserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mComics.size();
        }

        @Override
        public Object getItem(int position) {
            return mComics.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Comic comic = mComics.get(position);

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.card_group, parent, false);
            }

            ImageView groupImageView = (ImageView)convertView.findViewById(R.id.card_group_imageview);

            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic))
                    .into(groupImageView);

            TextView tv = (TextView) convertView.findViewById(R.id.comic_group_folder);
            tv.setText(comic.getFile().getParentFile().getName());

            return convertView;
        }
    }
}
