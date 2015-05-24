package com.nkanaev.comics.parsers;

import java.io.File;
import java.io.IOException;

public class ParserFactory {

    public static Parser create(String file) {
        return create(new File(file));
    }

    public static Parser create(File file) {
        Parser parser = null;
        String fileName = file.getAbsolutePath().toLowerCase();
        if (file.isDirectory()) {
            parser = new DirectoryParser();
        }
        if (fileName.matches(".+(cbz|zip)$")) {
            parser = new ZipParser();
        }
        else if (fileName.matches(".+(cbr|rar)$")) {
            parser = new RarParser();
        }
        return tryParse(parser, file);
    }

    private static Parser tryParse(Parser parser, File file) {
        if (parser == null) {
            return null;
        }
        try {
            parser.parse(file);
        }
        catch (IOException e) {
            return null;
        }
        return parser;
    }
}
