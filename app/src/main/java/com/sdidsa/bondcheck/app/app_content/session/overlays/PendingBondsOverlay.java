package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.graphics.Color;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Refresh;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.scroll.Scroller;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondStatus;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.UserCard;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.UserCardMode;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.requests.BondObject;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class PendingBondsOverlay extends PartialSlideOverlay {

    private final Scroller preUsers;
    private final VBox users;
    private final ColoredLabel empty;
    private final Refresh refresh;
    private final ColoredSpinLoading loading;

    public PendingBondsOverlay(Context owner) {
        this(owner, null);
    }

    public PendingBondsOverlay(Context owner, BondStatus source) {
        super(owner, .6f);

        preUsers = new Scroller(owner);
        preUsers.setClipToOutline(true);
        preUsers.setClipChildren(false);

        preUsers.setOnRefreshGesture(this::onMaybeRefresh);
        preUsers.setOnRefresh(this::onRefresh);

        users = new VBox(owner);
        users.setSpacing(15);
        PaddingUtils.setPadding(users, 20, 5, 20, 20, owner);
        users.setClipToOutline(false);

        users.setLayoutParams(new LayoutParams(-1,-1));

        SpacerUtils.spacer(preUsers, Orientation.VERTICAL);

        loading = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);
        empty = new ColoredLabel(owner,
                Style.TEXT_SEC, "pending_empty"
        );
        empty.setFont(new Font(20));
        empty.centerText();

        Label title = new ColoredLabel(owner, Style.TEXT_NORM, "pending_header");
        title.setFont(new Font(22, FontWeight.MEDIUM));

        HBox top = new ColoredHBox(owner, Style.BACK_PRI);
        top.setCornerRadiusTop(20);
        PaddingUtils.setPadding(top, 20, 20, 20, 5, owner);
        top.setElevation(2000);
        top.setOutlineAmbientShadowColor(Color.TRANSPARENT);
        top.setAlignment(Alignment.CENTER);

        refresh = new Refresh(owner, Style.TEXT_NORM, 48);
        top.addViews(title, SpacerUtils.spacer(owner, Orientation.HORIZONTAL), refresh);

        list.addView(top);
        list.addView(preUsers);

        Runnable load = () -> {
            refresh.hide().start();
            Animation.sequenceFadeOutRight(owner, ContextUtils.getViewChildren(users))
                    .setOnFinished(() -> {
                        users.removeAllViews();

                        loading.startLoading();
                        preUsers.setContent(loading, Alignment.CENTER);

                        Call<List<BondObject>> call = App.api(owner).pending();

                        Service.enqueue(call, resp -> {
                            if (resp.isSuccessful()) {
                                List<BondObject> bonds = resp.body();
                                assert bonds != null;

                                if (bonds.isEmpty()) {
                                    refresh.show().start();
                                    loading.stopLoading();
                                    preUsers.setContent(empty, Alignment.CENTER);
                                } else {
                                    Platform.runBack(() -> {
                                        ArrayList<UserResponse> userList = new ArrayList<>();

                                        for (BondObject bond : bonds) {
                                            SessionService.getUser(owner, bond.getUserId1(), userList::add);
                                        }

                                        Platform.waitWhile(() -> userList.size() != bonds.size());
                                        for (UserResponse user : userList) {
                                            UserCard card = UserCard.make(owner, source, user,
                                                    UserCardMode.PENDING_MODE);
                                            card.setAlpha(0);
                                            card.setTranslationX(0);
                                            users.addView(card);
                                        }

                                        Platform.runLater(() -> {
                                            refresh.show().start();
                                            loading.stopLoading();

                                            preUsers.setContent(users);
                                            Animation.sequenceFadeInUp(owner,
                                                            ContextUtils.getViewChildren(users))
                                                    .start();
                                        });
                                    });
                                }
                            } else {
                                refresh.show().start();
                                preUsers.removeView(loading);
                                loading.stopLoading();
                                ContextUtils.toast(owner, "problem_string");
                            }
                        });
                    }).start();

        };

        refresh.setOnClick(load);
        addOnShowing(load);
    }

    public void onMaybeRefresh(float dist) {
        refresh.applyRefresh(dist);
    }

    public void onRefresh() {
        refresh.fire();
        new AlphaAnimation(300, preUsers, 1)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    public void removeUser(UserCard userCard) {
        users.removeView(userCard);
        if (users.getChildCount() == 0) {
            preUsers.setContent(empty, Alignment.CENTER);
        }
    }
}
