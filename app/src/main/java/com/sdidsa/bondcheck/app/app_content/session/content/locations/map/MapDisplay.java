package com.sdidsa.bondcheck.app.app_content.session.content.locations.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.core.content.res.ResourcesCompat;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.models.DBLocation;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.CustomZoomButtonsDisplay;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

public class MapDisplay extends ColoredStackPane {
    private final MapView mapView;
    private final IMapController mapController;

    private final LayerSelector layerSelect;
    private final Marker startMarker;

    public MapDisplay(Context owner) {
        super(owner, Style.BACK_PRI);

        Configuration.getInstance().setUserAgentValue(owner.getPackageName());
        mapView = new MapView(owner);

        mapView.setTileProvider(new MapTileProviderBasic(owner));
        mapView.setTilesScaledToDpi(true);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel(18.0);
        mapController = mapView.getController();
        mapView.getZoomController().getDisplay().setPositions(false,
                CustomZoomButtonsDisplay.HorizontalPosition.RIGHT,
                CustomZoomButtonsDisplay.VerticalPosition.BOTTOM);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setDestroyMode(false);

        Drawable icon = ResourcesCompat.getDrawable(owner.getResources(),
                R.drawable.map_marker, null);
        int size = ContextUtils.dipToPx(48, owner);
        Bitmap markerBmp = Bitmap.createScaledBitmap(drawableToBitmap(icon), size, size, true);

        startMarker = new Marker(mapView);
        Drawable mark = new BitmapDrawable(owner.getResources(), markerBmp);
        startMarker.setIcon(mark);
        startMarker.setOnMarkerClickListener((marker, map) -> true);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(mRotationGestureOverlay);

        MapButton layers = new MapButton(owner, R.drawable.layers);
        layers.setOffset(0, 0);

        MapButton center = new MapButton(owner, R.drawable.location_fill);
        center.setOffset(1, 0);
        center.setOnClick(this::center);

        layerSelect = new LayerSelector(owner, mapView);

        mapView.setTileSource(TileSourceFactory.MAPNIK);

        addView(mapView);
        addAligned(layers, Alignment.TOP_RIGHT);
        addAligned(center, Alignment.TOP_RIGHT);

        layers.setOnClick(() -> {
            if(layerSelect.isAttachedToWindow()){
                hideLayerSelect();
            } else {
                showLayerSelect();
            }
        });

        setOutlineProvider(new OutlineProvider());
        setClipToOutline(true);
    }

    public void hideLayerSelect() {
        Animation.fadeOutRight(owner, layerSelect)
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> removeView(layerSelect)).start();
    }

    public void showLayerSelect() {
        addAligned(layerSelect, Alignment.TOP_RIGHT, 0);
        layerSelect.setOffset(0, 1);
        Animation.fadeInLeft(owner, layerSelect)
                .setInterpolator(Interpolator.OVERSHOOT)
                .start();
    }

    public void load(DBLocation location) {
        GeoPoint center = new GeoPoint(location.latitude(),
                location.longitude());
        mapController.setZoom(15.0);
        mapController.setCenter(center);
        mapView.setTilesScaleFactor(ContextUtils.scale);
        startMarker.setPosition(center);
        postInvalidate();
    }

    private void center() {
        mapController.animateTo(startMarker.getPosition(), 17d, 500L, 0f);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private class OutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0,0,
                    view.getWidth(), view.getHeight()), ContextUtils.dipToPx(15, owner));
        }
    }
}
