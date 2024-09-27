package com.sdidsa.bondcheck.app.app_content.session.content.locations.map;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.sdidsa.bondcheck.BuildConfig;
import com.sdidsa.bondcheck.abs.components.controls.image.Image;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;

public class MapTileStyle extends HBox {
    private static OnlineTileSourceBase createTileSource(String styleName) {
        return new OnlineTileSourceBase(styleName,
                0, 18, 512, "",
                new String[] {
                        "https://tiles-eu.stadiamaps.com/tiles/{style}/{z}/{x}/{y}@2x.png" +
                                "?api_key=" + BuildConfig.STADIA_API_KEY}) {
            @Override
            public String getTileURLString(long index) {
                return getBaseUrl().replace("{z}", MapTileIndex.getZoom(index) + "")
                        .replace("{x}", MapTileIndex.getX(index) + "")
                        .replace("{y}", MapTileIndex.getY(index) + "")
                        .replace("{style}", styleName);
            }
        };
    }

    public static final OnlineTileSourceBase ALIDADE_SATELLITE =
            createTileSource("alidade_satellite");
    public static final OnlineTileSourceBase OSM_BRIGHT =
            createTileSource("osm_bright");
    public static final OnlineTileSourceBase STAMEN_TONER_LITE =
            createTileSource("stamen_toner_lite");

    public MapTileStyle(Context owner) {
        this(owner, null, TileSourceFactory.MAPNIK, "Mapnik");
    }

    public MapTileStyle(Context owner, MapView mapView, OnlineTileSourceBase tileSource, String textString) {
        super(owner);
        setAlignment(Alignment.CENTER_LEFT);

        Image prev = new Image(owner);
        prev.setSize(42);
        prev.setCornerRadius(8);

        Platform.runBack(() -> {
            MapTileProviderBasic prov = new MapTileProviderBasic(owner);
            prov.setTileSource(tileSource);
            Drawable d = null;
            while(d == null) {
                d = prov.getMapTile(4611704252174197840L);
            }
            Drawable finalD = d;
            Platform.runLater(() -> prev.setImageBitmap(((BitmapDrawable) finalD).getBitmap()));
        });

        ColoredLabel text = new ColoredLabel(owner, Style.TEXT_NORM, textString)
                .setFont(new Font(18));

        ContextUtils.setMarginLeft(text, owner, 10);

        setOnClickListener(e -> {
            mapView.setTileSource(tileSource);
            VBox parent = (VBox) getParent();
            MapDisplay grandParent = (MapDisplay) parent.getParent();
            grandParent.hideLayerSelect();
        });

        addViews(prev, text);

        //setEnabled(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : .5f);
        setClickable(enabled);
    }
}
