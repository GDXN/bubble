package com.nkanaev.comics.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;


public interface Parser {
    void parse(File file) throws IOException;
    void destroy() throws IOException;

    String getType();
    InputStream getPage(int num) throws IOException;
    int numPages();
}
