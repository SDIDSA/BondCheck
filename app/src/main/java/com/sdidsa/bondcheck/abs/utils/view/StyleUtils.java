package com.sdidsa.bondcheck.abs.utils.view;

import android.content.Context;
import android.content.res.Configuration;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Store;

public class StyleUtils {
    private static Property<Style> style;
    public static Property<Style> getStyle(Context context) {
        if(context instanceof App app) {
            return app.getStyle();
        }else {
            if(style == null) {
                style = new Property<>(readStyle(context));
            }
            return style;
        }
    }

    public static void styleChanged(Context context) {
        getStyle(context).set(readStyle(context));
    }

    private static Style readStyle(Context context) {
        String theme = Store.getTheme();
        String s = theme.equals(Style.THEME_DARK) ? "dark" :
                theme.equals(Style.THEME_LIGHT) ? "light" :
                        isDarkMode(context.getResources().getConfiguration()) ?
                                "dark" : "light";

        return new Style(context, s, s.equalsIgnoreCase("dark"));
    }

    public static void setStyle(Context context, Style style) {
        getStyle(context).set(style);
    }


    public static boolean isDarkMode(Configuration newConfig) {
        return (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void applyTheme(Context context) {
        if(context instanceof App app) {
            app.applyTheme();
        }
    }
}
