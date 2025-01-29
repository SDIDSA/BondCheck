package com.sdidsa.bondcheck.app.app_content.session.content.locations;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.location.AddressProxy;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.models.responses.LocationResponse;

public class LocationView extends ItemView implements Localized {

    public synchronized static LocationView make(Context owner, LocationResponse location) {
        LocationView view = instance(owner, LocationView.class);
        view.loadLocation(location);
        return view;
    }

    private LocationResponse data;

    public LocationView(Context owner) {
        super(owner);

        setPadding(15);
        setCornerRadius(15);

        ColoredIcon img = new ColoredIcon(owner, Style.TEXT_SEC, Style.BACK_SEC,
                R.drawable.map_marker);
        img.setCornerRadius(10);
        img.setPadding(HomeSection.ITEM_SIZE / 3.2f);
        img.setSize(HomeSection.ITEM_SIZE);
        addViews(img);

        applyLocale(LocaleUtils.getLocale(owner));
    }

    private void loadLocation(LocationResponse location) {
        super.loadItem(location);
        this.data = location;

        second.setText("");

        AddressProxy.getAddress(location.getLocation(),
                LocaleUtils.getLocale(owner).get().getLang(), second::setText);

        setOnClickListener(e -> LocationOverlay.getInstance(owner).show(location));
    }

    @Override
    public void applyLocale(Locale locale) {
        if(data != null) {
            AddressProxy.getAddress(data.getLocation(),
                LocaleUtils.getLocale(owner).get().getLang(), second::setText);
        }
    }

}
