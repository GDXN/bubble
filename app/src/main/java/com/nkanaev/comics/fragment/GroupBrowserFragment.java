package com.nkanaev.comics.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.widget.*;
import android.support.v4.app.Fragment;

import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.MainActivity;
import com.nkanaev.comics.managers.LocalCoverHandler;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.view.CoverImageView;


public class GroupBrowserFragment extends Fragment {
    private ArrayList<Comic> mComics;
    private File[] mDirs;
    private HashMap<File, ArrayList<Comic>> mGroups;

    public GroupBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity)getActivity();
        final View view = inflater.inflate(R.layout.fragment_groupbrowser, container, false);

        getLibrary();

        final GridView gridView = (GridView)view.findViewById(R.id.groupGridView);
        final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        gridView.setAdapter(new GroupBrowserAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = mDirs[position].getAbsolutePath();
                BrowserFragment browserFragment = BrowserFragment.create(path);
                activity.pushFragment(browserFragment, true);
            }
        });
        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_group_column_width);
        int numColumns = Math.round((float)deviceWidth / columnWidth);
        gridView.setNumColumns(numColumns);

        final MainActivity.OnRefreshListener listener = new MainActivity.OnRefreshListener() {
            @Override
            public void onRefreshStart() {
                gridView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRefreshEnd() {
                gridView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                getLibrary();
                GroupBrowserAdapter adapter = (GroupBrowserAdapter)gridView.getAdapter();
                adapter.notifyDataSetChanged();
            }
        };
        activity.setOnRefreshListener(listener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.browser, menu);
    }

    private void getLibrary() {
        MainActivity activity = (MainActivity)getActivity();
        mComics = Storage.getStorage(getActivity()).listComics();
        mGroups = new HashMap<File, ArrayList<Comic>>();
        for (Comic c : mComics) {
            File dir = c.getFile().getParentFile();

            ArrayList<Comic> group = null;
            if (mGroups.containsKey(dir)) {
                group = mGroups.get(dir);
            }
            else {
                group = new ArrayList<Comic>();
                mGroups.put(dir, group);
            }
            group.add(c);
        }
        mDirs = mGroups.keySet().toArray(new File[mGroups.size()]);
    }

    private final class GroupBrowserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return (mDirs != null) ? mDirs.length : 0;
        }

        @Override
        public Object getItem(int position) {
            return mDirs[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            File groupDir = mDirs[position];
            Comic comic = mGroups.get(groupDir).get(0);

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.card_group, parent, false);
            }

            ImageView groupImageView = (ImageView)convertView.findViewById(R.id.card_group_imageview);

            ((MainActivity)getActivity()).getPicasso()
                    .load(LocalCoverHandler.getComicCoverUri(comic))
                    .into(groupImageView);

            TextView tv = (TextView)convertView.findViewById(R.id.comic_group_folder);
            tv.setText(groupDir.getName());

            return convertView;
        }
    }
}
