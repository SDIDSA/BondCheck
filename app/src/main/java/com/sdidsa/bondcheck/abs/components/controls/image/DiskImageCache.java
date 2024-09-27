package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskImageCache {

    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private final File diskCacheDir;

    public DiskImageCache(Context context, String uniqueName,
                          Bitmap.CompressFormat compressFormat, int quality) {
        diskCacheDir = getDiskCacheDir(context, uniqueName);
        mCompressFormat = compressFormat;
        mCompressQuality = quality;

        if (!diskCacheDir.exists() || !diskCacheDir.isDirectory()) {
            if (!diskCacheDir.mkdir()) {
                ErrorHandler.handle(new RuntimeException("failed to create cache dir"),
                        "init disk cache");
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    public File put(String key, Bitmap data) {
        File saveTo = keyToFile(key);
        try (OutputStream os = new FileOutputStream(saveTo)){
            data.compress(mCompressFormat, mCompressQuality, os);
        } catch (IOException e) {
            ErrorHandler.handle(e, "caching image with key [ " + key + " ]");
        }
        return saveTo;
    }

    public Bitmap getBitmap(String key) {
        File readFrom = keyToFile(key);
        if(exists(key)) {
            try (InputStream is = new FileInputStream(readFrom)) {
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public boolean exists(String key) {
        File readFrom = keyToFile(key);
        return readFrom.exists() && readFrom.isFile();
    }

    private File keyToFile(String key) {
        return new File(diskCacheDir.getAbsolutePath() + File.separator + key);
    }

}