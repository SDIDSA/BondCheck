package com.sdidsa.bondcheck.app.app_content.session.content.locations.map;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.DBLocation;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.CustomZoomButtonsDisplay;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

public class MapDisplay extends ColoredStackPane {
    private final MapView mapView;
    private final IMapController mapController;

    private final LayerSelector layerSelect;

    private final CircleOverlay userMarker;

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

        userMarker = new CircleOverlay(owner, mapView, Style.ACCENT);

        mapView.getOverlays().add(userMarker);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(mRotationGestureOverlay);

        MapButton layers = new MapButton(owner, R.drawable.layers);
        layers.setOffset(0, 0);

        MapButton center = new MapButton(owner, R.drawable.location_fill);
        center.setOffset(1, 0);
        center.setOnClick(this::center);

        layerSelect = new LayerSelector(owner, mapView);

        mapView.setTileSource(MapTileStyle.OSM_BRIGHT);

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
        layerSelect.setAlpha(0);
        Animation.fadeInLeft(owner, layerSelect)
                .setInterpolator(Interpolator.OVERSHOOT)
                .start();
    }

    public void load(DBLocation location, String user_id) {
        GeoPoint center = new GeoPoint(location.latitude(),
                location.longitude());
        mapController.setZoom(15.0);
        mapController.setCenter(center);
        mapView.setTilesScaleFactor(SizeUtils.scale);

        float sizeDp = 48;
        float borderDp = 10;
        int size = (SizeUtils.dipToPx(sizeDp, owner) / 4) * 4;
        int border = ((size + SizeUtils.dipToPx(borderDp, owner)) / 4) * 4;
        userMarker.setGeoPoint(center);
        userMarker.setRadiusPx(border / 2);
        mapView.postInvalidate();
        SessionService.getAvatar(owner, user_id, size, (bmp) -> {
            RoundedBitmapDrawable mark = RoundedBitmapDrawableFactory.create(owner.getResources(), bmp);
            mark.setCornerRadius(size / 2f);
            userMarker.setAvatar(bmp);
            mapView.postInvalidate();
        });
    }

    private void center() {
        double currentZoom = mapView.getZoomLevelDouble();
        double targetZoom = 17d;
        long minDuration = 500L;
        long maxDuration = 2000L;

        double zoomDifference = Math.abs(currentZoom - targetZoom);

        double normalizedDifference = Math.min(zoomDifference / 10, 1);

        long duration = (long) (minDuration + (normalizedDifference * (maxDuration - minDuration)));

        mapController.animateTo(userMarker.getGeoPoint(), 17d, duration, 0f);
    }

    private class OutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0,0,
                    view.getWidth(), view.getHeight()), SizeUtils.dipToPx(15, owner));
        }
    }
}
