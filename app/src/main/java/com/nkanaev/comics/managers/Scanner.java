package com.nkanaev.comics.managers;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import android.os.AsyncTask;
import android.content.Context;
import com.nkanaev.comics.model.*;
import com.nkanaev.comics.parsers.Parser;
import com.nkanaev.comics.parsers.ParserBuilder;

public class Scanner extends AsyncTask<File, Integer, Long> {

    private Context mContext;
//    private Bookshelf mBookshelf;
    private LocalUpdater mUpdater;

    public Scanner(Context context) {
        mContext = context;
        mUpdater = new LocalUpdater(Storage.getStorage(context));
    }

    @Override
    protected Long doInBackground(File... rootDirs) {
        File rootDir = rootDirs[0];
        long result = 0;

        Deque<File> files = new ArrayDeque<File>();
        files.push(rootDir);

        while (!files.isEmpty()) {
            File f = files.pop();
            for (File file : f.listFiles()) {
                if (file.isDirectory()) {
                    files.add(file);
                }

                Parser parser = new ParserBuilder(file).build();
                if (parser == null) {
                    continue;
                }

                if (parser.numPages() > 0) {
                    mUpdater.addBook(file.getAbsolutePath(), parser.getType(), parser.numPages());
                }
            }
        }
        mUpdater.commit();

        return result;
    }
}
