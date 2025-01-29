package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.AlignUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondState;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.http.services.SessionService;

public class BondSettingsOverlay extends PartialSlideOverlay {
    private final NetImage pfp;

    DestroyBondOverlay destroyOverlay;
    public BondSettingsOverlay(Context owner) {
        super(owner, .6f);

        list.setAlignment(Alignment.TOP_CENTER);

        StackPane sp = new ColoredStackPane(owner, Style.ACCENT);
        sp.setCornerRadiusTop(15);

        VBox noBond = new VBox(owner);
        noBond.setSpacing(20);
        noBond.setAlignment(Alignment.CENTER);
        AlignUtils.alignInFrame(noBond, Alignment.CENTER);

        ColorIcon noBondIcon = new ColorIcon(owner,
                R.drawable.heart_broken, 72);
        noBondIcon.setFill(Color.WHITE);
        Label noBondLabel = new Label(owner,
                "no_bond");
        noBondLabel.setFill(Color.WHITE);
        noBondLabel.setFont(new Font(20));
        noBond.addViews(noBondIcon, noBondLabel);

        int sizeDp = 128;
        int size = SizeUtils.dipToPx(sizeDp + 60, owner);
        sp.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, size));

        pfp = new NetImage(owner);
        pfp.setSize(sizeDp);
        pfp.setCornerRadius(sizeDp);

        ColoredIcon state = new ColoredIcon(owner,
                s -> Color.WHITE,
                Style.ACCENT,R.drawable.heart);

        state.setSize(sizeDp / 2.5f);
        state.setCornerRadius(sizeDp / 2f);
        int dt = SizeUtils.dipToPx(sizeDp / 3,owner);
        state.setTranslationX(dt * LocaleUtils.getLocaleDirection(owner));
        state.setTranslationY(dt);
        state.setPadding(12);

        AlignUtils.alignInFrame(pfp, Alignment.CENTER);
        AlignUtils.alignInFrame(state, Alignment.CENTER);

        VBox content = new VBox(owner);
        content.setPadding(20);
        content.setSpacing(20);

        Button notifications = createButton("notification_settings", R.drawable.bell);
        Button privacy = createButton("privacy_settings", R.drawable.shield);
        Button destroy = createButton("destroy_bond", R.drawable.heart_broken);
        Button findBond = createButton("find_your", R.drawable.heart);
        Button cancelRequest = createButton("cancel_request", R.drawable.heart_broken);

        notifications.setOnClick(() -> {
            addOnHiddenOnce(() ->
                    Platform.runBack(() -> {
                        Home home = Page.getInstance(owner, Home.class);
                        assert home != null;
                        home.getNavBar().getSettingsItem().select(
                                () -> getSettings().getNotificationsGroup().openIfClosed());
                    }));

            hide();
        });

        privacy.setOnClick(() -> {
            addOnHiddenOnce(() ->
                    Platform.runBack(() -> {
                        Home home = Page.getInstance(owner, Home.class);
                        assert home != null;
                        home.getNavBar().getSettingsItem().select(
                                () -> getSettings().getPrivacyGroup().openIfClosed());
                    }));

            hide();
        });

        destroy.setOnClick(() -> {
            if(destroyOverlay == null) {
                destroyOverlay = new DestroyBondOverlay(owner);
                destroyOverlay.setOnSuccess(this::hide);
            }

            destroyOverlay.show();
        });

        findBond.setOnClick(() -> {
            addOnHiddenOnce(() -> {
                Home home = Page.getInstance(owner, Home.class);
                assert home != null;
                home.getNavBar().getHomeItem().select(
                        () -> {
                            Main main = Fragment.getInstance(owner, Main.class);
                            assert main != null;
                            main.getBondStatus().getAction2().fire();
                        });
            });

            hide();
        });

        cancelRequest.setOnClick(findBond::fire);

        list.addView(sp);
        list.addView(content);

        addOnShowing(() -> {
            sp.removeAllViews();
            content.removeAllViews();

            Main main = Fragment.getInstance(owner, Main.class);
            assert main != null;
            String user_id = main.getBondStatus().getOther_user();
            BondState bondState = main.getBondStatus().getBondState();
            if(bondState == BondState.REQUEST_SENT) {
                user_id = "";
                noBondLabel.setKey("You sent them a bond request");
                noBondIcon.setImageResource(R.drawable.heart_pulse);
                content.addView(cancelRequest);
            }else if(bondState == BondState.NO_BOND) {
                user_id = "";
                noBondLabel.setKey("You don't have a bond...");
                noBondIcon.setImageResource(R.drawable.heart_broken);
                content.addView(findBond);
            }

            if(user_id.isEmpty()) {
                sp.addView(noBond);
            }else {
                sp.addView(pfp);
                sp.addView(state);
                content.addView(notifications);
                content.addView(privacy);
                content.addView(destroy);
                state.setAlpha(0);
                pfp.startLoading();

                SessionService.getUser(owner, user_id, user -> {
                    if(user.getAvatar() != null) {
                        ImageProxy.getImage(owner, user.getAvatar(), bmp -> {
                            pfp.setImageBitmap(bmp);
                            state.setAlpha(1f);
                        });
                    }
                });
            }
        });
    }

    private Settings getSettings() {
        return Fragment.getInstance(owner, Settings.class);
    }

    private Button createButton(String text, @DrawableRes int icon) {
        ColoredButton res = new ColoredButton(owner, Style.BACK_SEC,
                Style.TEXT_NORM, text);
        res.setFont(new Font(18, FontWeight.MEDIUM));
        res.setElevation(0);

        res.addPostLabel(SpacerUtils.spacer(owner, Orientation.HORIZONTAL));
        res.addPostLabel(new ColoredIcon(owner, Style.TEXT_SEC, icon, 22));

        return res;
    }
}
