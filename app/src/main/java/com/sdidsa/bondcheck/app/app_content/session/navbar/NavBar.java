package com.sdidsa.bondcheck.app.app_content.session.navbar;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.history.History;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.Locations;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.content.records.Records;
import com.sdidsa.bondcheck.app.app_content.session.content.screenshots.Screenshots;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;

public class NavBar extends StackPane implements Styleable {
    public static final float SELECTED_ITEM_HEIGHT = 90;
    public static final float ITEM_HEIGHT = SELECTED_ITEM_HEIGHT * 0.8f;
    public static final float ICON_SIZE = ITEM_HEIGHT * 0.4f;

    private final NavBarItem homeItem;
    private final NavBarItem screensItem;
    private final NavBarItem microphoneItem;
    private final NavBarItem locationItem;
    private final NavBarItem settingsItem;
    private final NavBarItem historyItem;

    private final SelectedBack back;
    private final HBox root;

    private NavBarItem selected;

    public NavBar(Context owner) {
        this(owner, null);
    }

    public NavBar(Context owner, Home home) {
        super(owner);

        setClipToPadding(false);
        setClipChildren(false);

        root = new HBox(owner);

        back = new SelectedBack(owner);

        homeItem = new NavBarItem(owner, this, home, R.drawable.home_fill,
                R.drawable.home_outline,
                Main.class);

        historyItem = new NavBarItem(owner, this, home, R.drawable.history_fill,
                R.drawable.history_outline,
                History.class);

        screensItem = new NavBarItem(owner, this, home, R.drawable.mobile_fill,
                R.drawable.mobile_outline,
                Screenshots.class);

        settingsItem = new NavBarItem(owner, this, home, R.drawable.cog_fill,
                R.drawable.cog_outline,
                Settings.class);

        microphoneItem = new NavBarItem(owner, this, home, R.drawable.mic_fill,
                R.drawable.mic_outline,
                Records.class);

        locationItem = new NavBarItem(owner, this, home, R.drawable.location_fill,
                R.drawable.location_outline,
                Locations.class);

        screensItem.setVisibility(INVISIBLE);
        microphoneItem.setVisibility(INVISIBLE);
        locationItem.setVisibility(INVISIBLE);
        historyItem.setVisibility(INVISIBLE);


        screensItem.setAlpha(0);
        microphoneItem.setAlpha(0);
        locationItem.setAlpha(0);
        historyItem.setAlpha(0);

        addView(back);
        addView(root);

        int total = ContextUtils.getScreenWidth(owner);
        int restW = (int) (total / 3f);

        homeItem.setWidth(restW);
        back.applyClip(true, restW);
        settingsItem.setWidth(restW + 5);
        historyItem.setWidth(restW);

        root.addView(historyItem);
        root.addView(homeItem);
        root.addView(settingsItem);

        homeItem.select();

        applyStyle(StyleUtils.getStyle(owner));
    }

    public HBox getRoot() {
        return root;
    }

    public SelectedBack getBack() {
        return back;
    }

    private Animation hideBond;
    public Animation hideBond() {
        if(hideBond == null) {
            hideBond = Animation.fadeOutDown(
                            owner, historyItem)
                    .setOnFinished(() -> historyItem.setVisibility(INVISIBLE));
        }
        return hideBond;
    }

    private Animation showBond;
    public Animation showBond() {
        if(showBond == null) {
            showBond = Animation.fadeInUp(
                    owner, historyItem)
                    .setBefore(() -> historyItem.setVisibility(VISIBLE));
        }
        return showBond;
    }

    public void setSelected(NavBarItem selected) {
        this.selected = selected;
    }

    public NavBarItem getSelected() {
        return selected;
    }

    public NavBarItem getHomeItem() {
        return homeItem;
    }

    public NavBarItem getScreensItem() {
        return screensItem;
    }

    public NavBarItem getSettingsItem() {
        return settingsItem;
    }

    public NavBarItem getMicrophoneItem() {
        return microphoneItem;
    }

    public NavBarItem getLocationItem() {
        return locationItem;
    }

    public NavBarItem getHistoryItem() {
        return historyItem;
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(style.getBackgroundSecondary());
    }

}
