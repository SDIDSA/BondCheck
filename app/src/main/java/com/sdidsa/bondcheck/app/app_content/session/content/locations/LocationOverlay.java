package com.sdidsa.bondcheck.app.app_content.session.content.locations;

import android.content.Context;

import com.sdidsa.bondcheck.abs.UiCache;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemDetailsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlayHeader;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.map.MapDisplay;
import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.responses.LocationResponse;

import java.util.ArrayList;

public class LocationOverlay extends PartialSlideOverlay implements ItemOverlay {

    private static final ArrayList<LocationOverlay> cache = new ArrayList<>();
    public static LocationOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        LocationOverlay found = null;
        for(LocationOverlay inst : cache) {
            if(!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if(found == null) {
            found = new LocationOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    public static void clearCache() {
        cache.clear();
    }

    static {
        UiCache.register(LocationOverlay::clearCache);
    }

    private Item loaded;

    private final ItemOverlayHeader top;
    private final MapDisplay mapView;

    private LocationOverlay(Context owner) {
        super(owner, .6f);

        list.setSpacing(20);
        list.setPadding(20);
        list.setClipChildren(false);

        top = new ItemOverlayHeader(owner);
        top.setTitle("item_location");
        top.hideSave();

        top.setOnClose(this::hide);
        top.setOnInfo(() -> ItemDetailsOverlay.getInstance(owner).show(loaded));

        mapView = new MapDisplay(owner);

        list.addView(top);
        list.addView(mapView);
    }

    private void load(Item locationFor) {
        load(locationFor, false);
    }

    private void load(Item locationFor, boolean related) {
        loaded = locationFor;
        load();
        top.showInfo(locationFor instanceof LocationResponse && !related);
    }

    private void load() {
        DBLocation dbLocation = loaded.getLocation();
        if(dbLocation == null) return;
        mapView.load(dbLocation, loaded.provider());
    }

    public void show(Item item) {
        super.show();
        Platform.runLater(() -> load(item));
    }

    @Override
    public void show(Item item, boolean related) {
        super.show();
        Platform.runLater(() -> load(item, related));
    }

    @Override
    public void show() {
        ErrorHandler.handle(new IllegalAccessError(
                        "can't show without loading a location object, " +
                                "use show(LocationResponse) or show(DBLocation) " +
                                "instead"),
                "showing LocationOverlay");
    }

}
