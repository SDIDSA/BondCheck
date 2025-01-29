package com.sdidsa.bondcheck.app.app_content.session.content.settings;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.about.AboutGroup;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.account.AccountGroup;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.display.DisplayGroup;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.notifications.NotificationsGroup;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy.PrivacyGroup;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.SettingsGroup;

public class Settings extends HomePage {

    private final AccountGroup account;
    private final NotificationsGroup notifications;
    private final PrivacyGroup privacy;
    private final DisplayGroup displayGroup;
    private final AboutGroup aboutGroup;

    private SettingsGroup open;

    public Settings(Context owner) {
        super(owner, "settings");

        account = new AccountGroup(owner, this);
        notifications = new NotificationsGroup(owner, this);
        privacy = new PrivacyGroup(owner, this);
        displayGroup = new DisplayGroup(owner, this);
        aboutGroup = new AboutGroup(owner, this);

        ColoredButton logout = new ColoredButton(owner, Style.BACK_SEC,
                Style.TEXT_ERR, "log_out");
        logout.setFont(new Font(20, FontWeight.MEDIUM));
        logout.setPadding(20);
        logout.extendLabel();
        logout.addPostLabel(new ColoredIcon(owner, Style.TEXT_ERR, R.drawable.logout, 26)
                .setAutoMirror(true));
        MarginUtils.setMargin(logout, owner, 15, 0, 15, 15);
        logout.setOnClick(() -> {
            Home home = Page.getInstance(owner, Home.class);
            assert home != null;
            home.exit();
        });

        addGroup(account);
        addGroup(notifications);
        addGroup(privacy);
        addGroup(displayGroup);
        addGroup(aboutGroup);

        content.setPadding(15);
        content.setSpacing(15);

        root.addViews(logout);
    }

    @Override
    public void setup(boolean direction) {
        super.setup(direction);

        Animation.sequenceFadeInUp(owner, ContextUtils.getViewChildren(content)).start();
    }

    @Override
    public void destroy(boolean direction) {
        super.destroy(direction);

        if(open != null) {
            open.close();
            open = null;
        }
    }

    public void scrollTo(SettingsGroup group) {
        int[] loc = new int[2];
        content.getLocationOnScreen(loc);
        scrollable.smoothScrollTo(0, group.calcY() - loc[1]);
    }

    public AccountGroup getAccountGroup() {
        return account;
    }

    public NotificationsGroup getNotificationsGroup() {
        return notifications;
    }

    public PrivacyGroup getPrivacyGroup() {
        return privacy;
    }

    public DisplayGroup getDisplayGroup() {
        return displayGroup;
    }

    public SettingsGroup getOpen() {
        return open;
    }

    public void setOpen(SettingsGroup open) {
        this.open = open;
    }

    private void addGroup(SettingsGroup group) {
        content.addView(group);
    }
}
