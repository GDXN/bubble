package com.nkanaev.comics.fragment;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;
import android.support.v4.app.Fragment;

import com.nkanaev.comics.Constants;
import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.LibraryBrowserActivity;
import com.nkanaev.comics.managers.LocalCoverHandler;
import com.nkanaev.comics.managers.Scanner;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.view.DirectorySelectDialog;
import com.squareup.picasso.Picasso;


public class LibraryFragment extends Fragment
        implements DirectorySelectDialog.OnDirectorySelectListener, AdapterView.OnItemClickListener {
    private final static String BUNDLE_DIRECTORY_DIALOG_SHOWN = "BUNDLE_DIRECTORY_DIALOG_SHOWN";

    private ArrayList<Comic> mComics;
    private DirectorySelectDialog mDirectorySelectDialog;
    private View mFirstLaunchView;
    private GridView mGridView;
    private Scanner mScanner;
    private ProgressBar mProgressBar;
    private Picasso mPicasso;

    private boolean mFirstLaunch = false;

    public LibraryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getActivity();
        mPicasso = new Picasso.Builder(ctx)
                .addRequestHandler(new LocalCoverHandler(ctx))
                .build();

        mComics = Storage.getStorage(getActivity()).listDirectoryComics();
        if (mComics.size() == 0) {
            SharedPreferences preferences = getActivity()
                    .getSharedPreferences(Constants.SETTINGS_NAME, 0);
            mFirstLaunch = !preferences.contains(Constants.SETTINGS_LIBRARY_DIR);
        }
    }

    @Override
    public void onDestroy() {
        mPicasso.shutdown();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_groupbrowser, container, false);

        mGridView = (GridView) view.findViewById(R.id.groupGridView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mGridView.setAdapter(new GroupBrowserAdapter());
        mGridView.setOnItemClickListener(this);

        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_group_column_width);
        int numColumns = Math.round((float) deviceWidth / columnWidth);
        mGridView.setNumColumns(numColumns);

        if (mFirstLaunch) {
            mFirstLaunchView = inflater.inflate(R.layout.library_firstlaunch, (ViewGroup) view, false);
            mDirectorySelectDialog = new DirectorySelectDialog(getActivity());
            mDirectorySelectDialog.setCurrentDirectory(Environment.getExternalStorageDirectory());
            mDirectorySelectDialog.setOnDirectorySelectListener(this);

            ((ViewGroup) view).addView(mFirstLaunchView);

            if (savedInstanceState != null && savedInstanceState.getBoolean(BUNDLE_DIRECTORY_DIALOG_SHOWN)) {
                mDirectorySelectDialog.show();
            }

            Button selectButton = (Button) mFirstLaunchView.findViewById(R.id.choose_dir_button);
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDirectorySelectDialog.show();
                }
            });
        }

        return view;
    }

    @Override
    public void onDirectorySelect(File file) {
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constants.SETTINGS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.SETTINGS_LIBRARY_DIR, file.getAbsolutePath());
        editor.apply();

        refreshLibrary(file);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Comic comic = mComics.get(position);

        Intent intent = new Intent(getActivity(), LibraryBrowserActivity.class);
        intent.putExtra(LibraryBrowserActivity.DIRECTORY, comic.getFile().getParent());
        startActivity(intent);
    }

    private void refreshLibrary(File file) {

        if (mScanner == null || mScanner.getStatus() == AsyncTask.Status.FINISHED) {

            mScanner = new Scanner(Storage.getStorage(getActivity())) {
                @Override
                protected void onPreExecute() {
                    if (mFirstLaunchView != null) {
                        ((ViewGroup)mFirstLaunchView.getParent()).removeView(mFirstLaunchView);
                        mFirstLaunchView = null;
                    }
                    mProgressBar.setVisibility(View.VISIBLE);
                    mComics = new ArrayList<>();
                    mGridView.requestLayout();
                }

                @Override
                protected void onPostExecute(Long aLong) {
                    ((ViewGroup)mProgressBar.getParent()).removeView(mProgressBar);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mComics = Storage.getStorage(getActivity()).listDirectoryComics();
                    mGridView.requestLayout();
                }
            };
            mScanner.execute(file);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_DIRECTORY_DIALOG_SHOWN,
                (mDirectorySelectDialog != null) && mDirectorySelectDialog.isShowing());
        super.onSaveInstanceState(outState);
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
