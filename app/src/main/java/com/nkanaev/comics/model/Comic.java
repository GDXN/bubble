package com.nkanaev.comics.model;

import java.io.File;
import android.net.Uri;


public class Comic {
    private Storage mShelf;
    private int mCurrentPage;
    private int mNumPages;
    private int mId;
    private String mFilePath;
    private String mType;

    public Comic(Storage shelf, int id, String filepath, String type, int numPages, int currentPage) {
        mShelf = shelf;
        mId = id;
        mNumPages = numPages;
        mCurrentPage = currentPage;
        mFilePath = filepath;
        mType = type;
    }

    public int getId() {
        return mId;
    }

    public Uri getPageUri(int pageNum) {
        return new Uri.Builder()
                .scheme(mType)
                .authority("/")
                .path(mFilePath)
                .fragment(Integer.toString(pageNum))
                .build();
    }

    public File getFile() {
        return new File(mFilePath);
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getTotalPages() {
        return mNumPages;
    }

    public void setCurrentPage(int page) {
        mShelf.bookmarkPage(getId(), page);
        mCurrentPage = page;
    }

    public String getType() {
        return mType;
    }
}