package com.sdidsa.bondcheck.app.app_content.session.content.main.create.location;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

public enum LocationType {
    VENUE("venue", R.drawable.venue),
    LOCALITY("locality", R.drawable.locality);

    private final String name;
    private final @DrawableRes int icon;

    LocationType(String name, @DrawableRes int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public static LocationType fromString(String name) {
        for (LocationType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        ErrorHandler.handle(new EnumConstantNotPresentException(LocationType.class, name),
                "get location type from string");
        return null;
    }
}
