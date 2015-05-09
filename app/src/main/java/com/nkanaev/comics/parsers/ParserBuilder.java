package com.nkanaev.comics.parsers;

import java.io.File;
import java.io.IOException;

public class ParserBuilder {
    private File mFile;

    public ParserBuilder(File file) {
        mFile = file;
    }

    public ParserBuilder(String path) {
        mFile = new File(path);
    }

    public Parser build() {
        Parser parser = null;
        String fileName = mFile.getAbsolutePath().toLowerCase();
        if (fileName.matches(".+(cbz|zip)$")) {
            parser = new ZipParser();
        }
        else if (fileName.matches(".+(cbr|rar)$")) {
            parser = new RarParser();
        }
        return tryParse(parser);
    }

    public Parser buildForType(String type) {
        Parser parser = null;
        if ("zip".equals(type)) {
            parser = new ZipParser();
        }
        else if ("rar".equals(type)) {
            parser = new RarParser();
        }
        else if ("dir".equals(type)) {
            parser = new FolderParser();
        }
        return tryParse(parser);
    }

    private Parser tryParse(Parser parser) {
        if (parser == null) {
            return null;
        }
        try {
            parser.parse(mFile);
        }
        catch (IOException e) {
            return null;
        }
        return parser;
    }
}
