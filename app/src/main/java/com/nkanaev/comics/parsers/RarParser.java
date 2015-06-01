package com.nkanaev.comics.parsers;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.nkanaev.comics.managers.Utils;


public class RarParser implements Parser {
    private ArrayList<FileHeader> mHeaders = new ArrayList<FileHeader>();
    private Archive mArchive;
    private File mCacheDir;
    private final Object mSync = new Object();

    @Override
    public void parse(File file) throws IOException {
        try {
            mArchive = new Archive(file);
        }
        catch (RarException e) {
            throw new IOException("unable to open archive");
        }

        FileHeader header = mArchive.nextFileHeader();
        while (header != null) {
            if (!header.isDirectory()) {
                String name = getName(header);
                if (Utils.isImage(name)) {
                    mHeaders.add(header);
                }
            }

            header = mArchive.nextFileHeader();
        }

        Collections.sort(mHeaders, new Comparator<FileHeader>() {
            public int compare(FileHeader a, FileHeader b) {
                return getName(a).compareTo(getName(b));
            }
        });
    }

    private String getName(FileHeader header) {
        return header.isUnicode() ? header.getFileNameW() : header.getFileNameString();
    }

    @Override
    public int numPages() {
        return mHeaders.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        try {
            FileHeader header = mHeaders.get(num);

            if (mCacheDir != null) {
                String name = getName(header);
                File cacheFile = new File(mCacheDir, Utils.MD5(name));
                synchronized (mSync) {
                    if (!cacheFile.exists()) {
                        FileOutputStream os = new FileOutputStream(cacheFile);
                        try {
                            mArchive.extractFile(header, os);
                        }
                        catch (Exception e) {
                            os.close();
                            cacheFile.delete();
                            throw e;
                        }
                        os.close();
                    }
                }
                return new FileInputStream(cacheFile);
            }
            return mArchive.getInputStream(header);
        }
        catch (RarException e) {
            throw new IOException("unable to parse rar");
        }
    }

    @Override
    public void destroy() throws IOException {
        if (mCacheDir != null) {
            for (File f : mCacheDir.listFiles()) {
                f.delete();
            }
            mCacheDir.delete();
        }
        mArchive.close();
    }

    @Override
    public String getType() {
        return "rar";
    }

    public void setCacheDirectory(File cacheDirectory) {
        mCacheDir = cacheDirectory;
        if (!mCacheDir.exists()) {
            mCacheDir.mkdir();
        }
        if (mCacheDir.listFiles() != null) {
            for (File f : mCacheDir.listFiles()) {
                f.delete();
            }
        }
    }
}
