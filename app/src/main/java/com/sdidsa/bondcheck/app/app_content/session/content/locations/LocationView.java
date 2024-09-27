package com.sdidsa.bondcheck.app.app_content.session.content.locations;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.location.AddressProxy;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.models.responses.LocationResponse;

import java.util.ArrayList;

public class LocationView extends ItemView implements Localized {
    private static final ArrayList<LocationView> cache = new ArrayList<>();

    public synchronized static LocationView make(Context owner, LocationResponse location) {
        cache.removeIf(item -> item.getOwner() != owner);

        LocationView view = null;
        for(LocationView c : cache) {
            if(c.getParent() == null) {
                view = c;
                break;
            }
        }

        if(view == null) {
            view = new LocationView(owner);
            cache.add(view);
        }

        view.loadLocation(location);

        return view;
    }

    private LocationResponse data;

    private LocationView(Context owner) {
        super(owner);

        setPadding(15);
        setCornerRadius(15);

        ColoredIcon img = new ColoredIcon(owner, Style.TEXT_SEC, Style.BACK_SEC,
                R.drawable.map_marker);
        img.setCornerRadius(10);
        img.setPadding(HomeSection.ITEM_SIZE / 3.2f);
        img.setSize(HomeSection.ITEM_SIZE);
        addViews(img);

        applyLocale(ContextUtils.getLocale(owner));
    }

    private void loadLocation(LocationResponse location) {
        super.loadItem(location);
        this.data = location;

        second.setText("");

        AddressProxy.getAddress(location.getLocation(),
                ContextUtils.getLocale(owner).get().getLang(), second::setText);

        setOnClickListener(e -> LocationOverlay.getInstance(owner).show(location));
    }

    @Override
    public void applyLocale(Locale locale) {
        if(data != null) {
            AddressProxy.getAddress(data.getLocation(),
                ContextUtils.getLocale(owner).get().getLang(), second::setText);
        }
    }

    @Override
    public void applyLocale(Property<Locale> locale) {
        Localized.bindLocale(this, locale);
    }
}
