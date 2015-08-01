package com.nkanaev.comics.fragment;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.ReaderActivity;
import com.nkanaev.comics.managers.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class BrowserFragment extends Fragment
        implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private File mCurrentDir;
    private File mRootDir;
    private File[] mSubdirs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootDir = Environment.getExternalStorageDirectory();
        setCurrentDir(mRootDir);

        getActivity().setTitle(R.string.menu_browser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        mListView = (ListView) view.findViewById(R.id.listview_browser);
        mListView.setAdapter(new DirectoryAdapter());
        mListView.setOnItemClickListener(this);

        return view;
    }

    private void setCurrentDir(File dir) {
        mCurrentDir = dir;
        ArrayList<File> subdirs = new ArrayList<>();
        if (!mCurrentDir.getAbsolutePath().equals(mRootDir.getAbsolutePath())) {
            subdirs.add(mCurrentDir.getParentFile());
        }
        for (File f : mCurrentDir.listFiles()) {
            if (f.isDirectory() || Utils.isArchive(f.getName())) {
                subdirs.add(f);
            }
        }
        Collections.sort(subdirs);
        mSubdirs = subdirs.toArray(new File[subdirs.size()]);

        if (mListView != null) {
            mListView.invalidateViews();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = mSubdirs[position];
        if (file.isDirectory()) {
            setCurrentDir(file);
        }
        else {
            Intent intent = new Intent(getActivity(), ReaderActivity.class);
            intent.putExtra(ReaderFragment.PARAM_HANDLER, file);
            intent.putExtra(ReaderFragment.PARAM_MODE, ReaderFragment.Mode.MODE_BROWSER);
            startActivity(intent);
        }
    }

    private void setIcon(View convertView, File file) {
        ImageView view = (ImageView) convertView.findViewById(R.id.directory_row_icon);
        int colorRes = R.color.circle_grey;
        if (file.isDirectory()) {
            view.setImageResource(R.drawable.ic_folder_white_24dp);
        }
        else {
            view.setImageResource(R.drawable.ic_file_document_box_white_24dp);

            String name = file.getName();
            if (Utils.isZip(name)) {
                colorRes = R.color.circle_green;
            }
            else if (Utils.isRar(name)) {
                colorRes = R.color.circle_red;
            }
        }

        GradientDrawable shape = (GradientDrawable) view.getBackground();
        shape.setColor(getResources().getColor(colorRes));
    }

    private final class DirectoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSubdirs.length;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mSubdirs[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.row_directory, parent, false);
            }

            File file = mSubdirs[position];
            TextView textView = (TextView) convertView.findViewById(R.id.directory_row_text);

            if (position == 0 && !mCurrentDir.getAbsolutePath().equals(mRootDir.getAbsolutePath())) {
                textView.setText("..");
            }
            else {
                textView.setText(file.getName());
            }

            setIcon(convertView, file);

            return convertView;
        }
    }
}
