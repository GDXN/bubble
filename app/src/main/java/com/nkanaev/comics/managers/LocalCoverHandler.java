package com.nkanaev.comics.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.nkanaev.comics.Constants;
import com.nkanaev.comics.parsers.Parser;
import com.nkanaev.comics.parsers.ParserBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import com.nkanaev.comics.model.Comic;


public class LocalCoverHandler extends RequestHandler {

    private final static String HANDLER_URI = "localcover";
    private Context mContext;

    public LocalCoverHandler(Context context) {
        mContext = context;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return HANDLER_URI.equals(data.uri.getScheme());
    }

    @Override
    public Result load(Request data, int networkPolicy) throws IOException {
        String path = getCoverPath(data.uri);
        return new Result(BitmapFactory.decodeFile(path), Picasso.LoadedFrom.DISK);
    }

    private String getCoverPath(Uri comicUri) throws IOException {

        File coverFile = new File(mContext.getExternalCacheDir(), Utils.MD5(comicUri.getPath()));

        if (!coverFile.isFile()) {
            Parser parser = new ParserBuilder(comicUri.getPath()).buildForType(comicUri.getFragment());
            InputStream stream = parser.getPage(0);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            options.inSampleSize = Utils.calculateInSampleSize(options,
                    Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT);
            options.inJustDecodeBounds = false;
            stream.close();
            stream = parser.getPage(0);
            Bitmap result = BitmapFactory.decodeStream(stream, null, options);

            FileOutputStream outputStream = new FileOutputStream(coverFile);
            result.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.close();
        }

        return coverFile.getAbsolutePath();
    }


    public static Uri getComicCoverUri(Comic comic) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .path(comic.getFile().getAbsolutePath())
                .fragment(comic.getType())
                .build();
    }
}
