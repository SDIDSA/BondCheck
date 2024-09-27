package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.LruCache;
import android.util.Size;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.models.requests.AssetRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class ImageProxy {
    public static final int FILE_EXISTS = 0;
    public static final int FILE_SAVED = 1;
    public static final int FILE_ERROR = -1;

    private static final LruCache<String, Bitmap> memoryCache =
            new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
    private static DiskImageCache diskCache;

    public static void init(App owner) {
        diskCache = new DiskImageCache(owner, "bitmaps", Bitmap.CompressFormat.PNG, 80);
    }

    private static String makeKey(String url) {
        if(url.contains("://")) {
            StringBuilder sb = new StringBuilder();

            int endIndex = url.indexOf("?");
            endIndex = endIndex == -1 ? url.length() : endIndex;
            for (int i = Math.max(0, endIndex - 40); i < endIndex; i++) {
                char c = url.charAt(i);
                if (Character.isDigit(c) || Character.isLetter(c) || c == '_') {
                    sb.append(Character.toLowerCase(c));
                }
            }

            return sb.toString();
        }else {
            return url;
        }
    }

    static final HashMap<String, List<Consumer<Bitmap>>> waiters = new HashMap<>();
    public synchronized static void getImage(Context owner, String url, Consumer<Bitmap> onResult) {
        String key = makeKey(url);

        if(waiters.containsKey(key)) {
            Objects.requireNonNull(waiters.get(key)).add(onResult);
            return;
        }

        List<Consumer<Bitmap>> forThis = new ArrayList<>();
        forThis.add(onResult);
        waiters.put(key, forThis);

        Platform.runBack(() -> {
            Bitmap cachedBitmap = get(key);
            if (cachedBitmap != null) {
                waiters.remove(key);
                Platform.runLater(() -> forThis.forEach(w -> w.accept(cachedBitmap)));
                return;
            }

            Platform.runBack(() -> {
                Bitmap downloadedBitmap = null;
                try {
                    downloadedBitmap = download(owner, url);
                    if (downloadedBitmap != null) {
                        put(key, downloadedBitmap);
                    }
                } catch (Exception x) {
                    ErrorHandler.handle(x, "downloading image at " + url);
                }

                Bitmap finalBitmap = downloadedBitmap;
                Platform.runLater(() -> {
                    waiters.remove(key);
                    forThis.forEach(w -> w.accept(finalBitmap));
                });
            });
        });
    }

    public static void getImageThumb(Context owner, String url, int size, Consumer<Bitmap> onResult) {
        getImage(owner, url, bmp ->
                Platform.runBack(() -> {
                    String key = makeKey(url) + "_thumb_" + size;
                    Bitmap found = get(key);
                    if (found == null) {
                        found = generateThumbnail(bmp, size);
                        put(key, found);
                    }
                    Bitmap finalFound = found;
                    Platform.runLater(() -> onResult.accept(finalFound));
                })
        );
    }

    private static final Semaphore thumbSemaphore = new Semaphore(3);
    public static void getThumbnail(Context owner, Uri uri, int size, Consumer<Bitmap> onResult, Runnable onFail) {
        Platform.runBack(() -> {
            try {
                thumbSemaphore.acquire();
            } catch (InterruptedException e) {
                return;
            }
            String url = uri.toString();
            String key = makeKey(url) + "_thumb_" + size;
            Bitmap found = get(key);
            if (found == null) {
                try {
                    found = generateThumbnail(owner, uri, size);
                    put(key, found);
                } catch (Exception x) {
                    ErrorHandler.handle(x, "generating thumbnail of " + url);
                    Platform.runLater(onFail);
                    thumbSemaphore.release();
                    return;
                }
            }
            Bitmap finalFound = found;
            Platform.runLater(() -> {
                try {
                    onResult.accept(finalFound);
                } catch (Exception e) {
                    ErrorHandler.handle(e, "handling thumbnail of " + url);
                    onFail.run();
                }
            });
            thumbSemaphore.release();
        });
    }

    private static Bitmap generateThumbnail(Context owner, Uri uri, int size) throws IOException {
        Bitmap bmp;
        bmp = owner.getApplicationContext().getContentResolver()
                .loadThumbnail(uri, new Size(size, size), null);
        int bmpw = bmp.getWidth();
        int bmph = bmp.getHeight();
        if (bmpw == bmph) return bmp;
        boolean hor = bmpw > bmph;
        int ds = hor ? bmph : bmpw;
        int sx = hor ? (bmpw - ds) / 2 : 0;
        int sy = hor ? 0 : (bmph - ds) / 2;
        return Bitmap.createBitmap(bmp, sx, sy, ds, ds);
    }

    private static Bitmap generateThumbnail(Bitmap bmp, int size){
        int bmpw = bmp.getWidth();
        int bmph = bmp.getHeight();
        if (bmpw == bmph) return bmp;
        boolean hor = bmpw > bmph;
        int ds = hor ? bmph : bmpw;
        int sx = hor ? (bmpw - ds) / 2 : 0;
        int sy = hor ? 0 : (bmph - ds) / 2;
        return Bitmap.createScaledBitmap(Bitmap.createBitmap(bmp, sx, sy, ds, ds),
                size, size, true);
    }

    private static Bitmap get(String key) {
        Bitmap found = memoryCache.get(key);
        if (found == null) {
            found = diskCache.getBitmap(key);
            if(found != null) {
                memoryCache.put(key, found);
            }
        }
        return found;
    }

    private static void put(String key, Bitmap bitmap) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, bitmap);
        }

        if (!diskCache.exists(key)) {
            diskCache.put(key, bitmap);
        }
    }

    private static Bitmap download(Context owner, String url) {
        if(url.contains("://")) {
            try (InputStream str = new URL(url.replace("http:", "https:"))
                    .openConnection().getInputStream()){
                return BitmapFactory.decodeStream(str);
            } catch (IOException e) {
                ErrorHandler.handle(e, "download image at " + url);
                return null;
            }
        }else {
            return downloadAsset(owner, url);
        }
    }

    private static Bitmap downloadAsset(Context owner, String assetId) {
        AssetRequest request = new AssetRequest(assetId, "image");
        Call<ResponseBody> call = App.api(owner).getAsset(request);

        try {
            Response<ResponseBody> response = call.execute();
            if(response.isSuccessful()) {
                ResponseBody body = response.body();
                if(body == null) throw new RuntimeException("failed to get Asset : " + response.code());
                try (InputStream str = body.byteStream()){
                    return BitmapFactory.decodeStream(str);
                }catch (Exception x) {
                    ErrorHandler.handle(x, "downloading asset " + assetId);
                }
                body.close();
            }else {
                ErrorHandler.handle(new RuntimeException("failed to get Asset : " + response.code()),
                        "downloading asset " + assetId);
            }
        }catch(Exception x) {
            ErrorHandler.handle(x, "downloading asset " + assetId);
        }
        return null;
    }

    public static File saveTemp(Bitmap bmp) {
        return diskCache.put("temp_" + System.currentTimeMillis(), bmp);
    }

    public static int saveImageToGallery(Context context, Bitmap bitmap, String fileName) {
        if(isFileInGallery(context, fileName)) {
            return FILE_EXISTS;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        values.put(MediaStore.Images.Media.IS_PENDING, true);
        final var contentResolver = context.getContentResolver();
        final var uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
        try {
            if(uri == null) throw new IllegalStateException("failed to open input stream");
            OutputStream fos = contentResolver.openOutputStream(uri);
            if(fos == null) throw new IllegalStateException("failed to open input stream");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            values.put(MediaStore.Images.Media.IS_PENDING, false);
            contentResolver.update(uri, values, null, null);
            return FILE_SAVED;
        }catch(Exception x) {
            ErrorHandler.handle(x, "saving image to storage");
            return FILE_ERROR;
        }
    }

    public static boolean isFileInGallery(Context context, String fileName) {
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{fileName};

        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
        }
        return false;
    }
}
