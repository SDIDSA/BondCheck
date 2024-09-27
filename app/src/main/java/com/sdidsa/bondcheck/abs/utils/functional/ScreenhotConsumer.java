package com.sdidsa.bondcheck.abs.utils.functional;

import android.graphics.Bitmap;

public interface ScreenhotConsumer {
    void accept(Bitmap image, String app);
}
