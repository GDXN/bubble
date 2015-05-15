package com.nkanaev.comics.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.LinearLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;

import com.nkanaev.comics.Constants;
import com.nkanaev.comics.R;
import com.nkanaev.comics.managers.LocalComicHandler;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.parsers.ParserBuilder;
import com.nkanaev.comics.view.PageImageView;
import com.nkanaev.comics.parsers.Parser;

import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

public class ReaderFragment extends Fragment {
    public static final int RESULT = 1;

    public static final String RESULT_CURRENT_PAGE = "fragment.reader.currentpage";

    public static final String PARAM_FILE = "readerFile";
    public static final String PARAM_PAGE = "readerOpenMode";

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
    private int mImageSize;
    private int mCurrentPage;
    private String mFilename;

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, Constants.PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constants.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constants.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constants.PageViewMode.FIT_WIDTH);
    }

    public static ReaderFragment create(String comicpath, int page) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_FILE, comicpath);
        args.putInt(PARAM_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    public ReaderFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getArguments().getString(PARAM_FILE);
        mCurrentPage = getArguments().getInt(PARAM_PAGE);
        File file = new File(path);
        mParser = new ParserBuilder(file).build();
        mFilename = file.getName();

        int width = Utils.getDeviceWidth(getActivity());
        int height = Utils.getDeviceHeight(getActivity());
        mImageSize = Math.max(width, height) * 2;

        mComicHandler = new LocalComicHandler(mParser, mImageSize);

        mPicasso = new Picasso.Builder(getActivity())
                .memoryCache(new LruCache(Utils.calculateMemorySize(getActivity(), 10)))
                .addRequestHandler(mComicHandler)
                .build();
        mPicasso.setLoggingEnabled(true);

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
                    setCurrentPage(getCurrentPage() - 1);
                }
                else if (x > (float)mViewPager.getWidth() / 3 * 2) {
                    setCurrentPage(getCurrentPage() + 1);
                }
                else {
                    setFullscreen(false, true);
                }
            }
        };

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reader, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setCurrentPage(position+1);
            }
        });

        if (mCurrentPage != -1) {
            setCurrentPage(mCurrentPage);
            mCurrentPage = -1;
        }
        setFullscreen(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);

        switch (mPageViewMode) {
            case ASPECT_FILL:
                menu.findItem(R.id.view_mode_aspect_fill).setChecked(true);
                break;
            case ASPECT_FIT:
                menu.findItem(R.id.view_mode_aspect_fit).setChecked(true);
                break;
            case FIT_WIDTH:
                menu.findItem(R.id.view_mode_fit_width).setChecked(true);
                break;
        }
    }

    @Override
    public void onDestroy() {
        try {
            mParser.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mPicasso.shutdown();
        super.onDestroy();
    }

    public int getCurrentPage() {
        return mViewPager.getCurrentItem() + 1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.view_mode_aspect_fill:
            case R.id.view_mode_aspect_fit:
            case R.id.view_mode_fit_width:
                item.setChecked(true);
                mPageViewMode = RESOURCE_VIEW_MODE.get(item.getItemId());
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(Constants.SETTINGS_PAGE_VIEW_MODE, mPageViewMode.native_int);
                editor.apply();
                updatePageViews(mViewPager);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCurrentPage(int page) {
        mViewPager.setCurrentItem(page - 1);
        String title = new StringBuilder()
                .append("(").append(page).append("/").append(mParser.numPages()).append(") ")
                .append(mFilename)
                .toString();
        getActionBar().setTitle(title);
    }

    private class ComicPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mParser.numPages();
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

            mPicasso.load(mComicHandler.getPageUri(position))
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
