package com.nkanaev.comics.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;


public interface Parser {
    public void parse(File file) throws IOException;

    public InputStream getPage(int num) throws IOException;
    public int numPages();
    public String getType();
    public void destroy() throws IOException;
}
