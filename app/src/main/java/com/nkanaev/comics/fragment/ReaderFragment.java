package com.nkanaev.comics.fragment;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.LinearLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;

import com.nkanaev.comics.Constants;
import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.MainActivity;
import com.nkanaev.comics.managers.LocalComicHandler;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.parsers.ParserBuilder;
import com.nkanaev.comics.view.PageImageView;
import com.nkanaev.comics.parsers.Parser;

import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ReaderFragment extends Fragment {
    private Comic mComic;
    private Parser mParser;
    private ViewPager mViewPager;
    private ComicPagerAdapter mPagerAdapter;
    private Picasso mPicasso;
    private LocalComicHandler mComicHandler;
    private Constants.PageViewMode mPageViewMode;
    private final static HashMap<Integer, Constants.PageViewMode> RESOURCE_VIEW_MODE;
    private SharedPreferences mPreferences;
    private PageImageView.OnPageTouchListener mPageTouchListener;
    private boolean mIsFullscreen;
    private int mImageWidth;
    private int mImageHeight;

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, Constants.PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constants.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constants.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constants.PageViewMode.FIT_WIDTH);
    }

    private final static String STATE_COMIC = "readerFragmentComic";

    public static ReaderFragment create(int comicId) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putInt(STATE_COMIC, comicId);
        fragment.setArguments(args);
        return fragment;
    }

    public ReaderFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int comicId = getArguments().getInt(STATE_COMIC);
        mComic = Storage.getStorage(getActivity()).getComic(comicId);
        mParser = new ParserBuilder(mComic.getFile()).buildForType(mComic.getType());

        mComicHandler = new LocalComicHandler(mComic, mParser);

        mPicasso = new Picasso.Builder(getActivity())
                .memoryCache(new LruCache(Utils.calculateMemorySize(getActivity(), 10)))
                .addRequestHandler(mComicHandler)
                .build();
        mPicasso.setLoggingEnabled(true);

        mImageWidth = Utils.getDeviceWidth(getActivity());
        mImageHeight = Utils.getDeviceHeight(getActivity());

        mPagerAdapter = new ComicPagerAdapter();

        mPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, 0);
        int viewModeInt = mPreferences.getInt(
                Constants.SETTINGS_PAGE_VIEW_MODE,
                Constants.PageViewMode.ASPECT_FIT.native_int);
        mPageViewMode = Constants.PageViewMode.values()[viewModeInt];

        mPageTouchListener = new PageImageView.OnPageTouchListener() {
            @Override
            public void onPageClicked(float x, float y) {
                if (!isFullscreen()) {
                    setFullscreen(true, true);
                    return;
                }

                if (x < (float)mViewPager.getWidth() / 3) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                }
                else if (x > (float)mViewPager.getWidth() / 3 * 2) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                }
                else {
                    setFullscreen(false, true);
                }
            }
        };

        setHasOptionsMenu(true);

        if (Utils.isLollipopOrLater()) {
            Window w = getActivity().getWindow();
            w.setStatusBarColor(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reader, container, false);

        mViewPager = (ViewPager)view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mComic.getCurrentPage() - 1);
        setFullscreen(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);
    }

    @Override
    public void onDestroy() {
        mComic.setCurrentPage(mViewPager.getCurrentItem() + 1);
        try {
            mParser.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mPicasso.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.view_mode_aspect_fill:
            case R.id.view_mode_aspect_fit:
            case R.id.view_mode_fit_width:
                mPageViewMode = RESOURCE_VIEW_MODE.get(item.getItemId());
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(Constants.SETTINGS_PAGE_VIEW_MODE, mPageViewMode.native_int);
                editor.apply();
                updatePageViews(mViewPager);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ComicPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mComic.getTotalPages();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LayoutInflater inflater = (LayoutInflater)getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.fragment_reader_page, container, false);
            PageImageView pageImageView = (PageImageView)layout.findViewById(R.id.comic_page);
            pageImageView.setViewMode(mPageViewMode);
            pageImageView.setOnPageTouchListener(mPageTouchListener);
            container.addView(layout);

            mPicasso.load(mComicHandler.getComicPageUri(position))
                    .resize(mImageWidth, mImageHeight)
                    .into(pageImageView, new MyCallback(pageImageView));

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout)object);
        }
    }

    private class MyCallback implements Callback {
        private PageImageView mImageView;

        public MyCallback(PageImageView imageView) {
            mImageView = imageView;
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onError() {
            mImageView.setImageResource(R.drawable.ic_refresh_white_24dp);
        }
    }

    private void updatePageViews(ViewGroup parentView) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            final View child = parentView.getChildAt(i);
            if (child instanceof ViewGroup) {
                updatePageViews((ViewGroup)child);
            }
            else if (child instanceof PageImageView) {
                ((PageImageView) child).setViewMode(mPageViewMode);
            }
        }
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity)getActivity()).getSupportActionBar();
    }

    private void setFullscreen(boolean fullscreen) {
        setFullscreen(fullscreen, false);
    }

    private void setFullscreen(boolean fullscreen, boolean animated) {
        mIsFullscreen = fullscreen;

        ActionBar actionBar = getActionBar();

        if (fullscreen) {
            if (actionBar != null) actionBar.hide();
        }
        else {
            if (actionBar != null) actionBar.show();
        }
    }

    private boolean isFullscreen() {
        return mIsFullscreen;
    }
}
