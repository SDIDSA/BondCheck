package com.sdidsa.bondcheck.app.app_content.session.content.settings.display;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.overlay.OverlayOption;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.MultipleChoiceSetting;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.UiScaleSetting;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.SettingsGroup;
import com.sdidsa.bondcheck.app.services.Action;

public class DisplayGroup extends SettingsGroup {
    public DisplayGroup(Context owner) {
        this(owner, null);
    }

    public DisplayGroup(Context owner, Settings parent) {
        super(owner, parent, "display_settings", R.drawable.monitor);

        addSetting(new MultipleChoiceSetting(owner, "app_theme", Store::getTheme,
                v -> Store.setTheme(v, s -> {
                    owner.sendBroadcast(App.broadcast(Action.STYLE_CHANGED));
                    Platform.runLater(() ->
                            StyleUtils.applyTheme(owner));
                }),
                new OverlayOption(Style.THEME_SYSTEM, R.drawable.sun_moon),
                new OverlayOption(Style.THEME_DARK, R.drawable.moon),
                new OverlayOption(Style.THEME_LIGHT, R.drawable.sun)
        ));

        addSetting(new MultipleChoiceSetting(owner, "animations", Store::getAnimations,
                v -> Store.setAnimations(v, Animation::applySpeed),
                new OverlayOption(Animation.OFF, R.drawable.stop),
                new OverlayOption(Animation.SLOW, R.drawable.snail),
                new OverlayOption(Animation.DEFAULT),
                new OverlayOption(Animation.FAST, R.drawable.fast)
        ));

        addSetting(new MultipleChoiceSetting(owner, "language", Store::getLanguage,
                v -> Store.setLanguage(v, s -> {
                    owner.sendBroadcast(App.broadcast(Action.LOCALE_CHANGED));
                    LocaleUtils.setLocale(owner, Locale.forName(owner, v));
                }),
                new OverlayOption("en_us"),
                new OverlayOption("fr_fr"),
                new OverlayOption("ar_ar"),
                new OverlayOption("de_DE")
        ));

        addSetting(new UiScaleSetting(owner, "ui_scale", () -> Store.getScale().getText(),
                v -> Store.setScale(v, s -> {
                    SizeUtils.scale = UiScale.forText(s).getScale();
                    ContextUtils.unloadApp(owner, () -> {
                        float oldTimeScale = Animation.timeScale;
                        Animation.timeScale = 0;
                        ContextUtils.loadPage(owner, Home.class, () -> {
                            Home home = Page.getInstance(owner, Home.class);
                            Platform.runAfter(() ->
                                    home.getNavBar().getSettingsItem().select(() -> {
                                        Animation.timeScale = oldTimeScale;
                                        ContextUtils.loadApp(owner, null);
                                        Fragment.getInstance(owner, Settings.class)
                                                .getDisplayGroup().open();
                            }), 50);
                        });
                    });
                }),
                new OverlayOption(UiScale.SMALLEST),
                new OverlayOption(UiScale.SMALLER),
                new OverlayOption(UiScale.NORMAL),
                new OverlayOption(UiScale.BIGGER),
                new OverlayOption(UiScale.BIGGEST)
        ));
    }
}
