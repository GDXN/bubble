package com.nkanaev.comics.fragment;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.*;
import android.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;

import com.nkanaev.comics.Constants;
import com.nkanaev.comics.R;
import com.nkanaev.comics.managers.LocalComicHandler;
import com.nkanaev.comics.managers.Utils;
import com.nkanaev.comics.model.Comic;
import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.parsers.ParserFactory;
import com.nkanaev.comics.parsers.RarParser;
import com.nkanaev.comics.view.PageImageView;
import com.nkanaev.comics.parsers.Parser;

import com.squareup.picasso.*;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;


public class ReaderFragment extends Fragment implements View.OnTouchListener {
    public static final int RESULT = 1;

    public static final String RESULT_CURRENT_PAGE = "fragment.reader.currentpage";

    public static final String PARAM_HANDLER = "PARAM_HANDLER";
    public static final String PARAM_MODE = "PARAM_MODE";

    private ViewPager mViewPager;
    private LinearLayout mPageNavLayout;
    private SeekBar mPageSeekBar;
    private TextView mPageNavTextView;
    private ComicPagerAdapter mPagerAdapter;
    private SharedPreferences mPreferences;
    private GestureDetector mGestureDetector;

    private final static HashMap<Integer, Constants.PageViewMode> RESOURCE_VIEW_MODE;
    private boolean mIsFullscreen;
    private int mCurrentPage;
    private String mFilename;
    private Constants.PageViewMode mPageViewMode;

    private Parser mParser;
    private Picasso mPicasso;
    private LocalComicHandler mComicHandler;
    private SparseArray<Target> mTargets = new SparseArray<>();

    private Comic mComic;

    public enum Mode {
        MODE_LIBRARY,
        MODE_BROWSER;
    }

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, Constants.PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constants.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constants.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constants.PageViewMode.FIT_WIDTH);
    }

    public static ReaderFragment create(int comicId) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MODE, Mode.MODE_LIBRARY);
        args.putInt(PARAM_HANDLER, comicId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReaderFragment create(File comicpath) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MODE, Mode.MODE_BROWSER);
        args.putSerializable(PARAM_HANDLER, comicpath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        Mode mode = (Mode) bundle.getSerializable(PARAM_MODE);

        File file = null;
        if (mode == Mode.MODE_LIBRARY) {
            int comicId = bundle.getInt(PARAM_HANDLER);
            mComic = Storage.getStorage(getActivity()).getComic(comicId);
            file = mComic.getFile();
            mCurrentPage = mComic.getCurrentPage();
        }
        else if (mode == Mode.MODE_BROWSER) {
            file = (File) bundle.getSerializable(PARAM_HANDLER);
        }
        mParser = ParserFactory.create(file);
        mFilename = file.getName();

        mCurrentPage = Math.max(1, Math.min(mCurrentPage, mParser.numPages()));

        mComicHandler = new LocalComicHandler(mParser);
        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(mComicHandler)
                .build();
        mPagerAdapter = new ComicPagerAdapter();
        mGestureDetector = new GestureDetector(getActivity(), new MyTouchListener());

        mPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, 0);
        int viewModeInt = mPreferences.getInt(
                Constants.SETTINGS_PAGE_VIEW_MODE,
                Constants.PageViewMode.ASPECT_FIT.native_int);
        mPageViewMode = Constants.PageViewMode.values()[viewModeInt];

        // workaround: extract rar achive
        if (mParser instanceof RarParser) {
            File cacheDir = new File(getActivity().getExternalCacheDir(), "c");
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
            else {
                for (File f : cacheDir.listFiles()) {
                    f.delete();
                }
            }
            ((RarParser)mParser).setCacheDirectory(cacheDir);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reader, container, false);

        mPageNavLayout = (LinearLayout) view.findViewById(R.id.pageNavLayout);
        mPageSeekBar = (SeekBar) mPageNavLayout.findViewById(R.id.pageSeekBar);
        mPageSeekBar.setMax(mParser.numPages() - 1);
        mPageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    setCurrentPage(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPicasso.pauseTag(ReaderFragment.this.getActivity());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPicasso.resumeTag(ReaderFragment.this.getActivity());
            }
        });
        mPageNavTextView = (TextView) mPageNavLayout.findViewById(R.id.pageNavTextView);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnTouchListener(this);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setCurrentPage(position + 1);
            }
        });

        if (Utils.isKitKatOrLater()) {
            Resources resources = getActivity().getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                int navBarheight = resources.getDimensionPixelSize(resourceId);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPageNavLayout.getLayoutParams();
                params.bottomMargin = navBarheight + 80;
                mPageNavLayout.setLayoutParams(params);
            }
        }

        if (mCurrentPage != -1) {
            setCurrentPage(mCurrentPage);
            mCurrentPage = -1;
        }
        setFullscreen(true);
        getActivity().setTitle(mFilename);

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
    public void onPause() {
        if (mComic != null) {
            mComic.setCurrentPage(getCurrentPage());
        }
        super.onPause();
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
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
        String navPage = new StringBuilder()
                .append(page).append("/").append(mParser.numPages())
                .toString();

        mPageNavTextView.setText(navPage);

        mPageSeekBar.setProgress(page-1);
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

            PageImageView pageImageView = (PageImageView) layout.findViewById(R.id.pageImageView);
            pageImageView.setViewMode(mPageViewMode);
            pageImageView.setOnTouchListener(ReaderFragment.this);

            container.addView(layout);

            Target t = new MyTarget(layout, position);
            loadImage(t, position);
            mTargets.put(position, t);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;
            mPicasso.cancelRequest(mTargets.get(position));
            mTargets.delete(position);
            container.removeView(layout);

            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            Drawable drawable = iv.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) drawable;
                Bitmap bm = bd.getBitmap();
                if (bm != null) {
                    bm.recycle();
                }
            }
        }
    }

    private void loadImage(Target t, int position) {
        mPicasso.load(mComicHandler.getPageUri(position))
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(getActivity())
                .into(t);
    }

    private class MyTarget implements Target, View.OnClickListener {
        private WeakReference<View> mLayout;
        private int mPosition;

        public MyTarget(View layout, int position) {
            mLayout = new WeakReference<>(layout);
            mPosition = position;
        }

        private void setVisibility(int imageView, int progressBar, int reloadButton) {
            View layout = mLayout.get();
            layout.findViewById(R.id.pageImageView).setVisibility(imageView);
            layout.findViewById(R.id.pageProgressBar).setVisibility(progressBar);
            layout.findViewById(R.id.reloadButton).setVisibility(reloadButton);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            View layout = mLayout.get();
            if (layout == null)
                return;

            setVisibility(View.VISIBLE, View.GONE, View.GONE);
            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            View layout = mLayout.get();
            if (layout == null)
                return;

            setVisibility(View.GONE, View.GONE, View.VISIBLE);

            ImageButton ib = (ImageButton) layout.findViewById(R.id.reloadButton);
            ib.setOnClickListener(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

        @Override
        public void onClick(View v) {
            View layout = mLayout.get();
            if (layout == null)
                return;

            setVisibility(View.GONE, View.VISIBLE, View.GONE);
            loadImage(this, mPosition);
        }
    }

    private class MyTouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isFullscreen()) {
                setFullscreen(true, true);
                return true;
            }

            float x = e.getX();

            if (x < (float) mViewPager.getWidth() / 3)
                setCurrentPage(getCurrentPage() - 1);
            else if (x > (float) mViewPager.getWidth() / 3 * 2)
                setCurrentPage(getCurrentPage() + 1);
            else
                setFullscreen(false, true);

            return true;
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
        return ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    private void setFullscreen(boolean fullscreen) {
        setFullscreen(fullscreen, false);
    }

    private void setFullscreen(boolean fullscreen, boolean animated) {
        mIsFullscreen = fullscreen;

        ActionBar actionBar = getActionBar();

        if (fullscreen) {
            if (actionBar != null) actionBar.hide();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.INVISIBLE);
        }
        else {
            if (actionBar != null) actionBar.show();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFullscreen() {
        return mIsFullscreen;
    }
}
