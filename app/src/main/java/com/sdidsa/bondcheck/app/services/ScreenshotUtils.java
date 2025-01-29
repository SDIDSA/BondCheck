package com.sdidsa.bondcheck.app.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.WindowManager;

import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.nio.ByteBuffer;

public class ScreenshotUtils {

    public static Bitmap addLogoToBlurredBitmap(Context context, Bitmap blurredBitmap, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Drawable logoDrawable = null;
        try {
            logoDrawable = packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            ErrorHandler.handle(e, "adding logo to screenshot");
        }
        if(logoDrawable == null) {
            return blurredBitmap;
        }
        Bitmap combinedBitmap = blurredBitmap.copy(blurredBitmap.getConfig(), true);
        Canvas canvas = new Canvas(combinedBitmap);

        int targetSize = Math.min(blurredBitmap.getHeight(), blurredBitmap.getWidth()) / 4;

        int left = (combinedBitmap.getWidth() - targetSize) / 2;
        int top = (combinedBitmap.getHeight() - targetSize) / 2;

        logoDrawable.setBounds(left, top, left + targetSize, top + targetSize);
        logoDrawable.draw(canvas);

        return combinedBitmap;
    }

    public static Bitmap imageToBitmap(Context context, Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        int captureWidth = width + rowPadding / pixelStride;

        Bitmap bitmap = Bitmap.createBitmap(captureWidth, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        Point size = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealSize(size);

        int targetWidth = width, targetHeight = height;
        int screenHeight = size.y;
        int screenWidth = size.x;
        float screenRatio = (float) screenWidth / screenHeight;
        if(screenHeight > screenWidth) {
            targetWidth = (int) (height * screenRatio);
        }else {
            targetHeight = (int) (width / screenRatio);
        }

        return resizeBitmap(Bitmap.createBitmap(bitmap,
                (width - targetWidth) / 2,
                (height - targetHeight) / 2,
                targetWidth, targetHeight), 1280, 1280);
    }

    public static Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float aspectRatio = (float) width / (float) height;

        int newWidth = maxWidth;
        int newHeight = maxHeight;

        if (width > height) {
            if (width > maxWidth) {
                newHeight = Math.round(newWidth / aspectRatio);
            }
        } else {
            if (height > maxHeight) {
                newWidth = Math.round(newHeight * aspectRatio);
            }
        }

        if (newWidth > width || newHeight > height) {
            return originalBitmap;
        }

        return ImageProxy.scale(originalBitmap, newWidth, newHeight);
    }

    public static Bitmap gaussianBlur(Bitmap src, int radius) {
        if (radius < 1) {
            return null;
        }

        Bitmap bitmap = src.copy(src.getConfig(), true);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int rsum, gsum, bsum, x, y, i, yi;
        int[] vmin = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yi = 0;
        int yw = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                int p = pixels[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                int p = pixels[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            int yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pixels[yi] = (0xff000000 & pixels[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                int p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
