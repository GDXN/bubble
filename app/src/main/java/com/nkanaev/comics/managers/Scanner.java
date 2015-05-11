package com.nkanaev.comics.managers;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import android.os.AsyncTask;
import com.nkanaev.comics.model.*;
import com.nkanaev.comics.parsers.Parser;
import com.nkanaev.comics.parsers.ParserBuilder;

public class Scanner extends AsyncTask<File, Integer, Long> {
    private Storage mStorage;

    public Scanner(Storage storage) {
        mStorage = storage;
    }

    @Override
    protected Long doInBackground(File... rootDirs) {
        File rootDir = rootDirs[0];
        long result = 0;

        Deque<File> files = new ArrayDeque<>();
        files.push(rootDir);

        mStorage.clearStorage();

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
                    mStorage.addBook(file, parser.getType(), parser.numPages());
                }
            }
        }

        return result;
    }
}
