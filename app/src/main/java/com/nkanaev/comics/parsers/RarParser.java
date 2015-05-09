package com.nkanaev.comics.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
                String name = header.isUnicode() ? header.getFileNameW() : header.getFileNameString();
                if (Utils.isImage(name)) {
                    mHeaders.add(header);
                }
            }

            header = mArchive.nextFileHeader();
        }

        Collections.sort(mHeaders, new Comparator<FileHeader>() {
            public int compare(FileHeader a, FileHeader b) {
                String nameA = a.isUnicode() ? a.getFileNameW() : a.getFileNameString();
                String nameB = b.isUnicode() ? b.getFileNameW() : b.getFileNameString();

                return nameA.compareTo(nameB);
            }
        });
    }

    @Override
    public int numPages() {
        return mHeaders.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        try {
            return mArchive.getInputStream(mHeaders.get(num));
        }
        catch (RarException e) {
            throw new IOException("unable to parse rar");
        }
    }

    @Override
    public void destroy() throws IOException {
        mArchive.close();
    }

    @Override
    public String getType() {
        return "rar";
    }
}
