package com.nkanaev.comics.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.nkanaev.comics.managers.Utils;


public class ZipParser implements Parser {
    private ZipFile mZipFile;
    private ArrayList<ZipEntry> mEntries;

    @Override
    public void parse(File file) throws IOException {
        mZipFile = new ZipFile(file.getAbsolutePath());
        mEntries = new ArrayList<ZipEntry>();

        Enumeration<? extends ZipEntry> e = mZipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            if (!ze.isDirectory() && Utils.isImage(ze.getName())) {
                mEntries.add(ze);
            }
        }

        Collections.sort(mEntries, new Comparator<ZipEntry>() {
            public int compare(ZipEntry a, ZipEntry b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    @Override
    public int numPages() {
        return mEntries.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return mZipFile.getInputStream(mEntries.get(num));
    }

    @Override
    public String getType() {
        return "zip";
    }

    @Override
    public void destroy() throws IOException {
        mZipFile.close();
    }
}
