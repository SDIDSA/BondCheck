package com.sdidsa.bondcheck.abs.components.controls.location;

import android.content.Context;

import com.sdidsa.bondcheck.abs.utils.Assets;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DiskAddressCache {

    private final File diskCacheDir;

    public DiskAddressCache(Context context, String uniqueName) {
        diskCacheDir = getDiskCacheDir(context, uniqueName);

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

    public void put(String key, String data) {
        File saveTo = keyToFile(key);
        Assets.writeFile(saveTo, data);
    }

    public String getAddress(String key) {
        File readFrom = keyToFile(key);
        if(exists(key)) {
            try (InputStream is = new FileInputStream(readFrom)) {
                return Assets.readFile(readFrom);
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
        return new File(diskCacheDir.getAbsolutePath() +
                File.separator + key + ".json");
    }

}