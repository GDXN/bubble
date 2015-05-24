package com.nkanaev.comics.parsers;


import com.nkanaev.comics.managers.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class DirectoryParser implements Parser {
    private ArrayList<File> mFiles = new ArrayList<>();

    @Override
    public void parse(File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                throw new IOException("Probably not a comic directory");
            }
            if (Utils.isImage(f.getAbsolutePath())) {
                mFiles.add(f);
            }
        }

        Collections.sort(mFiles);
    }

    @Override
    public int numPages() {
        return mFiles.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return new FileInputStream(mFiles.get(num));
    }

    @Override
    public String getType() {
        return "dir";
    }

    @Override
    public void destroy() throws IOException {
    }
}
