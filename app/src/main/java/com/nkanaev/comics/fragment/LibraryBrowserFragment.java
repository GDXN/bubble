package com.nkanaev.comics.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.*;
import android.widget.*;
import android.support.v7.widget.SearchView;

import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.ReaderActivity;
import com.nkanaev.comics.managers.LocalCoverHandler;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.view.CoverImageView;
import com.squareup.picasso.Picasso;

public class LibraryBrowserFragment extends Fragment
        implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {
    public static final String PARAM_PATH = "browserCurrentPath";

    private GridView mGridView;
    private ListAdapter mAdapter;
    private ArrayList<Comic> mComics;
    private ArrayList<Comic> mComicsFiltered;
    private Picasso mPicasso;
    private Comic mCurrentComic;
    private String mFilterSearch = "";
    private int mFilterRead = R.id.menu_browser_filter_all;


    public static LibraryBrowserFragment create(String path) {
        LibraryBrowserFragment fragment = new LibraryBrowserFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public LibraryBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getArguments().getString(PARAM_PATH);

        mComics = Storage.getStorage(getActivity()).listComics(path);
        Collections.sort(mComics);
        filterContent();
        Context ctx = getActivity();
        mPicasso = new Picasso.Builder(ctx)
                .addRequestHandler(new LocalCoverHandler(ctx))
                .build();

        getActivity().setTitle(new File(path).getName());

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        mPicasso.shutdown();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_librarybrowser, container, false);

        final BrowserAdapter adapter = new BrowserAdapter();
        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_comic_column_width);
        int numColumns = Math.round((float) deviceWidth / columnWidth);

        mGridView = (GridView)view.findViewById(R.id.gridView);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setNumColumns(numColumns);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ReaderFragment.RESULT) {
            int curPage = data.getIntExtra(ReaderFragment.RESULT_CURRENT_PAGE, -1);
            if (curPage != -1 && mCurrentComic != null) {
                mCurrentComic.setCurrentPage(curPage);
                mGridView.invalidateViews();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.browser, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_browser_filter_all:
            case R.id.menu_browser_filter_read:
            case R.id.menu_browser_filter_unread:
                item.setChecked(true);
                mFilterRead = item.getItemId();
                filterContent();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mFilterSearch = s;
        filterContent();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Comic comic = mComics.get(position);

        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderFragment.PARAM_FILE, comic.getFile().getAbsolutePath());
        intent.putExtra(ReaderFragment.PARAM_PAGE, comic.getCurrentPage());
        startActivityForResult(intent, ReaderFragment.RESULT);

        mCurrentComic = comic;
    }

    private void filterContent() {
        mComicsFiltered = new ArrayList<>();
        for (Comic c: mComics) {
            if (mFilterSearch.length() > 0 && !c.getFile().getName().contains(mFilterSearch))
                continue;
            if (mFilterRead != R.id.menu_browser_filter_all) {
                if (mFilterRead == R.id.menu_browser_filter_read && c.getCurrentPage() == 0)
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_unread && c.getCurrentPage() != 0)
                    continue;
            }
            mComicsFiltered.add(c);
        }

        if (mGridView != null) mGridView.invalidateViews();
    }

    private final class BrowserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mComicsFiltered.size();
        }

        @Override
        public Object getItem(int position) {
            return mComicsFiltered.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup comicView = (ViewGroup) convertView;
            if (comicView == null) {
                comicView = (ViewGroup)getActivity()
                        .getLayoutInflater()
                        .inflate(R.layout.card_comic, parent, false);
            }

            Comic comic = mComicsFiltered.get(position);

            CoverImageView coverImageView = (CoverImageView)comicView.findViewById(R.id.comicImageView);
            TextView titleTextView = (TextView)comicView.findViewById(R.id.comicTitleTextView);
            TextView pagesTextView = (TextView)comicView.findViewById(R.id.comicPagerTextView);

            titleTextView.setText(comic.getFile().getName());
            pagesTextView.setText(Integer.toString(comic.getCurrentPage()) + '/' + Integer.toString(comic.getTotalPages()));

            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic))
                    .into(coverImageView);

            return comicView;
        }
    }
}
