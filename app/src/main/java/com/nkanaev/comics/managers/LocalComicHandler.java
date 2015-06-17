package com.nkanaev.comics.managers;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.graphics.Bitmap;
import com.nkanaev.comics.parsers.*;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;


public class LocalComicHandler extends RequestHandler {
    private final static String HANDLER_URI = "localcomic";
    private Parser mParser;

    public LocalComicHandler(Parser parser) {
        mParser = parser;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        int pageNum = Integer.parseInt(request.uri.getFragment());
        InputStream stream = mParser.getPage(pageNum);
        return new Result(stream, Picasso.LoadedFrom.DISK);
    }

    public Uri getPageUri(int pageNum) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .fragment(Integer.toString(pageNum))
                .build();
    }
}
