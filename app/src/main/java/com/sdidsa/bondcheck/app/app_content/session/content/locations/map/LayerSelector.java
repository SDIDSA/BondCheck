package com.sdidsa.bondcheck.app.app_content.session.content.locations.map;

import android.content.Context;
import android.widget.FrameLayout;

import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredVBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class LayerSelector extends ColoredVBox implements Localized {
    private int horizontal = 0, vertical = 0;

    public LayerSelector(Context owner) {
        this(owner, null);
    }

    public LayerSelector(Context owner, MapView mapView) {
        super(owner, Style.BACK_PRI);
        setSpacing(10);
        setPadding(15);
        setCornerRadius(12);
        setElevation(SizeUtils.dipToPx(10, owner));

        MapTileStyle basic = new MapTileStyle(owner, mapView,
                TileSourceFactory.MAPNIK, "Basic");
        basic.setEnabled(true);

        MapTileStyle def = new MapTileStyle(owner, mapView,
                MapTileStyle.OSM_BRIGHT, "Bright");

        MapTileStyle satellite = new MapTileStyle(owner, mapView,
                MapTileStyle.ALIDADE_SATELLITE, "Satellite");

        MapTileStyle black_white = new MapTileStyle(owner, mapView,
                MapTileStyle.STAMEN_TONER_LITE, "Black & white");

        addViews(basic, def, /*satellite,*/ black_white);
        setLayoutParams(new FrameLayout.LayoutParams(-2, -2));

        applyLocale(LocaleUtils.getLocale(owner));
    }

    public void setOffset(int vertical, int horizontal) {
        this.horizontal = horizontal;
        this.vertical = vertical;

        applyLocale(LocaleUtils.getLocale(owner).get());
    }

    @Override
    public void applyLocale(Locale locale) {
        MarginUtils.setMarginTopRight(this, owner,
                (52 * vertical) + 10,
                (52 * horizontal) + 10);
    }

}
