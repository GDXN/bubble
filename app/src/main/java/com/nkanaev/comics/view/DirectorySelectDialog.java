package com.nkanaev.comics.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nkanaev.comics.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class DirectorySelectDialog
        extends Dialog
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Button mSetButton;
    private Button mCancelButton;
    private OnDirectorySelectListener mListener;
    private ListView mListView;
    private TextView mTitleTextView;
    private File mRootDir;
    private File mCurrentDir;
    private File[] mSubdirs;
    private FileFilter mDirectoryFilter;

    public interface OnDirectorySelectListener {
        public void onDirectorySelect(File file);
    }

    public DirectorySelectDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        setContentView(R.layout.dialog_directorypicker);
        mSetButton = (Button) findViewById(R.id.directory_picker_confirm);
        mCancelButton = (Button) findViewById(R.id.directory_picker_cancel);
        mListView = (ListView) findViewById(R.id.directory_listview);
        mTitleTextView = (TextView) findViewById(R.id.directory_current_text);

        mSetButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        mListView.setAdapter(new DirectoryListAdapter());
        mListView.setOnItemClickListener(this);

        mDirectoryFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
    }

    public void setCurrentDirectory(File path) {
        if (mCurrentDir == null) {
            mRootDir = path;
        }
        mCurrentDir = path;

        ArrayList<File> subDirs = new ArrayList<>(Arrays.asList(mCurrentDir.listFiles(mDirectoryFilter)));
        if (!mCurrentDir.getPath().equals(mRootDir.getPath())) {
            subDirs.add(0, mCurrentDir.getParentFile());
        }
        Collections.sort(subDirs);
        mSubdirs = subDirs.toArray(new File[subDirs.size()]);

        mTitleTextView.setText(mCurrentDir.getPath());
        mListView.requestLayout();
    }

    public void setOnDirectorySelectListener(OnDirectorySelectListener l) {
        mListener = l;
    }

    @Override
    public void onClick(View v) {
        if (v == mSetButton) {
            if (mListener != null) {
                mListener.onDirectorySelect(mCurrentDir);
            }
        }
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File dir = mSubdirs[position];
        setCurrentDirectory(dir);
    }

    private class DirectoryListAdapter extends BaseAdapter {
        @Override
        public Object getItem(int position) {
            return mSubdirs[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getCount() {
            return (mSubdirs != null) ? mSubdirs.length : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.row_directory, parent, false);
            }

            File dir = mSubdirs[position];
            TextView textView = (TextView) convertView.findViewById(R.id.directory_row_text);

            if (position == 0 && !mRootDir.getPath().equals(mCurrentDir.getPath())) {
                textView.setText("..");
            }
            else {
                textView.setText(dir.getName());
            }

            return convertView;
        }
    }
}
