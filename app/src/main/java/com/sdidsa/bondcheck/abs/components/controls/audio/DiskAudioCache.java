package com.sdidsa.bondcheck.abs.components.controls.audio;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.LruCache;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.utils.Assets;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.models.requests.AssetRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DiskAudioCache {
    private static final String LOUDNESS = "loudness";
    private static final String DURATION = "duration";

    private final File diskCacheDir;

    private static final LruCache<String, AudioFile> memoryCache =
            new LruCache<>(10);

    public DiskAudioCache(Context context, String uniqueName) {
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

    public AudioFile get(String key) {
        AudioFile found = memoryCache.get(key);
        if(found == null) {
            File readFrom = keyToFile(key);
            if (readFrom.exists()) {
                found = read(readFrom);
                if(found != null) {
                    memoryCache.put(key, found);
                }
            }
        }
        return found;
    }

    public AudioFile download(Context owner, String urlString, String key) {
        File saveTo = keyToFile(key);

        if (saveTo.exists()) {
            AudioFile res = read(saveTo);
            if(res != null) {
                return res;
            }
        }

        if(!download(owner, urlString, saveTo)) {
            return null;
        }

        long duration = getAudioDuration(saveTo);
        float loudness = AudioProcessing.getMaxLoudness(saveTo);

        try {
            JSONObject meta = new JSONObject();
            meta.put(DURATION, duration);
            meta.put(LOUDNESS, loudness);

            File metaData = new File(saveTo.getAbsolutePath() + ".meta");
            Assets.writeFile(metaData, meta.toString());

            AudioFile res = new AudioFile(saveTo, duration, loudness);
            memoryCache.put(key, res);
            return res;
        }catch (JSONException x) {
            ErrorHandler.handle(x, "writing meta data of " + saveTo.getName());
        }
        return null;
    }

    private boolean download(Context owner, String urlString, File saveTo) {
        if(urlString.contains("://")) {
            try(InputStream inputStream = new URL(urlString.replace("http:", "https:"))
                    .openConnection()
                    .getInputStream();
                OutputStream outputStream = new FileOutputStream(saveTo)) {

                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
                return true;
            } catch (Exception e) {
                ErrorHandler.handle(e, "download and cache audio");
                return false;
            }
        } else {
            AssetRequest request = new AssetRequest(urlString, "video");
            Call<ResponseBody> call = App.api(owner).getAsset(request);

            try {
                Response<ResponseBody> response = call.execute();
                if(response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if(body == null) throw new RuntimeException("failed to get Asset : " + response.code());
                    try(InputStream inputStream = body.byteStream();
                        OutputStream outputStream = new FileOutputStream(saveTo)) {

                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.flush();
                        body.close();
                        return true;
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "download and cache audio");
                        return false;
                    }
                }else {
                    ErrorHandler.handle(new RuntimeException("failed to get Asset : " + response.code()),
                            "downloading asset " + urlString);
                }
            }catch(Exception x) {
                ErrorHandler.handle(x, "downloading asset " + urlString);
            }
            return false;
        }
    }

    private AudioFile read(File file) {
        File metaData = new File(file.getAbsolutePath() + ".meta");

        if (file.exists() && metaData.exists()) {
            try {
                JSONObject meta = new JSONObject(Assets.readFile(metaData));
                long duration = meta.getLong(DURATION);
                float loudness = (float) meta.getDouble(LOUDNESS);

                return new AudioFile(file, duration, loudness);
            } catch (JSONException e) {
                ErrorHandler.handle(e, "parsing meta data of " + file.getName());
            }
        }
        return null;
    }

    public long getAudioDuration(File file) {
        try(MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(file.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                return Long.parseLong(durationStr);
            }
        } catch (Exception e) {
            ErrorHandler.handle(e, "getting duration of audio");
        }
        return -1;
    }

    private File keyToFile(String key) {
        return new File(diskCacheDir.getAbsolutePath() + File.separator + key + ".mp4");
    }

}