package com.nkanaev.comics.model;

import java.io.File;
import android.net.Uri;


public class Comic {
    private Storage mShelf;
    private int mCurrentPage;
    private int mNumPages;
    private int mId;
    private String mType;
    private File mFile;

    public Comic(Storage shelf, int id, String filepath, String filename,
                 String type, int numPages, int currentPage) {
        mShelf = shelf;
        mId = id;
        mNumPages = numPages;
        mCurrentPage = currentPage;
        mFile = new File(filepath, filename);
        mType = type;
    }

    public int getId() {
        return mId;
    }

    public File getFile() {
        return mFile;
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