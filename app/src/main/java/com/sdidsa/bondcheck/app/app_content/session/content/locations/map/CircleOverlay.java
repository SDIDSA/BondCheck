package com.sdidsa.bondcheck.app.app_content.session.content.locations.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class CircleOverlay extends Overlay implements Styleable {
    private final Context owner;
    private final Paint paint;

    private final MapView map;
    private GeoPoint geoPoint;
    private int radiusPx;

    private StyleToColor fill;

    private Bitmap avatar;
    private Bitmap circularAvatar;
    private final Point mPositionPixels;

    private Runnable onClick;

    public CircleOverlay(Context owner, MapView map, StyleToColor fill) {
        this.owner = owner;
        this.map = map;
        this.fill = fill;
        this.geoPoint = new GeoPoint(0f,0f);
        this.radiusPx = 0;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        mPositionPixels = new Point();

        applyStyle(StyleUtils.getStyle(owner));
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
        map.postInvalidate();
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setRadiusPx(int radiusPx) {
        this.radiusPx = radiusPx;
        map.postInvalidate();
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
        if (avatar != null) {
            createCircularAvatar();
        } else {
            circularAvatar = null;
        }
        map.postInvalidate();
    }

    private void createCircularAvatar() {
        if (avatar == null) return;

        int diameter = Math.min(avatar.getWidth(), avatar.getHeight());
        circularAvatar = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(circularAvatar);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Rect destRect = new Rect(0, 0, diameter, diameter);
        RectF destRectF = new RectF(destRect);

        canvas.drawCircle(diameter/2f, diameter/2f, diameter/2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Rect sourceRect = new Rect(
                (avatar.getWidth() - diameter) / 2,
                (avatar.getHeight() - diameter) / 2,
                (avatar.getWidth() + diameter) / 2,
                (avatar.getHeight() + diameter) / 2
        );

        canvas.drawBitmap(avatar, sourceRect, destRectF, paint);
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(StyleUtils.getStyle(owner).get());
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        if(onClick != null) onClick.run();
        return super.onSingleTapConfirmed(e, mapView);
    }

    @Override
    public void draw(Canvas canvas, Projection projection) {
        if(fill == null) return;

        Point screenPoint = new Point();
        projection.toPixels(geoPoint, screenPoint);
        projection.toPixels(geoPoint, mPositionPixels);
        canvas.rotate(-projection.getOrientation(), mPositionPixels.x, mPositionPixels.y);

        canvas.drawCircle(screenPoint.x, screenPoint.y, radiusPx, paint);

        if(circularAvatar != null) {
            canvas.drawBitmap(circularAvatar,
                    screenPoint.x - circularAvatar.getWidth() / 2f,
                    screenPoint.y - circularAvatar.getHeight() / 2f,
                    paint);
        }
    }

    @Override
    public void applyStyle(Style style) {
        paint.setColor(fill.get(style));
        map.postInvalidate();
    }
}