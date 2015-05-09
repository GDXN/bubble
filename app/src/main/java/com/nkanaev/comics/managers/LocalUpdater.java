package com.nkanaev.comics.managers;


import com.nkanaev.comics.model.Storage;
import com.nkanaev.comics.model.Comic;

import java.util.*;

public class LocalUpdater {
    private Storage mShelf;
    private ArrayList<Comic> mSavedComics;
    private HashMap<String, Comic> mStoredComics;
    private Set<String> mAddedPaths;
    private ArrayList<ArrayList<Object>> mNewComics;

    public LocalUpdater(Storage shelf) {
        mShelf = shelf;
        ArrayList<Comic> comics = mShelf.listComics();
        mStoredComics = new HashMap<String, Comic>();
        for (Comic comic : comics) {
            mStoredComics.put(comic.getFile().getAbsolutePath(), comic);
        }
        mAddedPaths = new HashSet<String>();
        mNewComics = new ArrayList<ArrayList<Object>>();
    }

    public void addBook(String filepath, String type, int numPages) {
        mAddedPaths.add(filepath);
        if (!mStoredComics.containsKey(filepath)) {
            ArrayList<Object> newComic = new ArrayList<Object>();
            newComic.add(filepath);
            newComic.add(type);
            newComic.add(numPages);
            mNewComics.add(newComic);
        }

    }

    public void commit() {
        // delete missing
        for (String filepath : mStoredComics.keySet()) {
            if (!mAddedPaths.contains(filepath)) {
                Comic missingComic = mStoredComics.get(filepath);
                mShelf.removeComic(missingComic.getId());
            }
        }

        // add new
        for (ArrayList<Object> comicInfo : mNewComics) {
            String filepath = (String)comicInfo.get(0);
            String type = (String)comicInfo.get(1);
            int numPages = (Integer)comicInfo.get(2);
            mShelf.addBook(filepath, type, numPages);
        }
    }
}
