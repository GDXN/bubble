package com.nkanaev.comics.fragment;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.MainActivity;
import com.nkanaev.comics.activity.ReaderActivity;
import com.nkanaev.comics.managers.LocalCoverHandler;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.view.CoverImageView;
import com.squareup.picasso.Picasso;

public class BrowserFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ArrayList<Comic> mComics;
    private String mPath;
    private Picasso mPicasso;
    private final static String STATE_PATH = "browserCurrentPath";

    public static BrowserFragment create(String path) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(STATE_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public BrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPath = getArguments().getString(STATE_PATH);

        mComics = Storage.getStorage(getActivity()).listComics(mPath);
        Collections.sort(mComics);
        Context ctx = getActivity();
        mPicasso = new Picasso.Builder(ctx)
                .addRequestHandler(new LocalCoverHandler(ctx))
                .build();
    }

    @Override
    public void onDestroy() {
        mPicasso.shutdown();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        GridView gridView = (GridView)view.findViewById(R.id.gridView);
        gridView.setAdapter(new BrowserAdapter());
        gridView.setOnItemClickListener(this);

        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_comic_column_width);
        int numColumns = Math.round((float)deviceWidth / columnWidth);
        gridView.setNumColumns(numColumns);

        return view;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Comic comic = mComics.get(position);

        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderActivity.PARAM_COMIC_ID, comic.getId());
        startActivity(intent);
    }

    private final class BrowserAdapter extends BaseAdapter {
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
            ViewGroup comicView = (ViewGroup) convertView;
            if (comicView == null) {
                comicView = (ViewGroup)getActivity()
                        .getLayoutInflater()
                        .inflate(R.layout.card_comic, parent, false);
            }

            Comic comic = mComics.get(position);

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
