package com.sdidsa.bondcheck.app.app_content.session.content.main.bond;

import android.content.Context;
import android.view.Gravity;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.base.ValueAnimation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.LinearHeightAnimation;
import com.sdidsa.bondcheck.abs.animation.view.padding.PaddingAnimation;
import com.sdidsa.bondcheck.abs.animation.view.padding.PaddingTopAnimation;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.overlays.MakeBondOverlay;
import com.sdidsa.bondcheck.app.app_content.session.overlays.PendingBondsOverlay;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketEventListener;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.CheckBondResponse;

import retrofit2.Call;

public class BondStatus extends HomeSection {
    private static final String BOND_ACCEPTED_EVENT = "bond_accepted";
    private static final String BOND_DESTROYED_EVENT = "bond_destroyed";

    private final Property<String> other_user;
    private final Property<BondState> bondState;

    private final ColoredIcon stateIcon;
    private final ColoredLabel stateLabel;
    private final ColoredButton action1, action2;

    private MakeBondOverlay makeBondOverlay;
    private PendingBondsOverlay pendingBondsOverlay;

    private boolean collapsed = false;

    private Animation collapse, expand;

    public BondStatus(Context owner) {
        super(owner, "bond_status", R.drawable.heart);

        HBox root = new HBox(owner);
        root.setPadding(5);
        root.setAlignment(Alignment.CENTER);

        bondState = new Property<>(BondState.NO_BOND);
        other_user = new Property<>("");

        stateIcon = new ColoredIcon(owner, Style.ACCENT,
                R.drawable.empty, 72);

        MarginUtils.setMarginLeft(stateIcon, owner, 15);

        stateLabel = new ColoredLabel(owner, Style.TEXT_NORM, "");
        stateLabel.setLines(2);
        stateLabel.setGravity(Gravity.TOP);
        stateLabel.setFont(new Font(18));

        action1 = new ColoredButton(owner, Style.BACK_SEC
                , Style.TEXT_NORM
                , "");
        action1.setElevation(0);
        PaddingUtils.setPaddingHorizontalVertical(action1, 0, 8, owner);

        action2 = new ColoredButton(owner, Style.BACK_SEC
                , Style.TEXT_NORM
                , "");
        PaddingUtils.setPaddingHorizontalVertical(action2, 0, 8, owner);
        action2.setElevation(0);

        SpacerUtils.spacer(action1);
        SpacerUtils.spacer(action2);
        MarginUtils.setMarginLeft(action2, owner, 10);

        action1.setFont(new Font(16));
        action2.setFont(new Font(16));

        VBox data = new VBox(owner);
        data.setSpacing(10);
        data.setAlignment(Alignment.CENTER_LEFT);


        HBox actions = new HBox(owner);
        actions.addViews(action1, action2);

        data.addView(stateLabel);
        data.addView(actions);

        SpacerUtils.spacer(data);

        root.addViews(data, stateIcon);

        SocketEventListener socket = Action.socketEventReceiver(owner);
        socket.on(BOND_ACCEPTED_EVENT, args -> {
            ContextUtils.toast(owner, "Your bond request was accepted");
            fetch();
        });

        socket.on(BOND_DESTROYED_EVENT, args -> {
            ContextUtils.toast(owner, "bond_destroyed");
            setBondStatus(BondState.NO_BOND);
        });

        Platform.waitWhileNot(this::isLaidOut, () -> Platform.runBack(() -> {
            int heightOpen = SizeUtils.dipToPx(160, owner);
            int heightClosed = SizeUtils.dipToPx(56, owner);
            int paddingTopOpen = SizeUtils.dipToPx(20, owner);

            float iconOpen = 72;
            float iconClose = 24;

            int[] paddingOpen = new int[]{14, 21, 14, 14};
            int[] paddingClose = new int[]{15, 11, 15, 10};

            paddingOpen = SizeUtils.dipToPx(paddingOpen, owner);
            paddingClose = SizeUtils.dipToPx(paddingClose, owner);

            float spacingOpen = 10;
            float buttonsOpen = SizeUtils.dipToPx(38, owner);

            collapse = new ParallelAnimation(300)
                    .addAnimation(new LinearHeightAnimation(this, heightOpen, heightClosed))
                    .addAnimation(new PaddingTopAnimation(this, paddingTopOpen, 0))
                    .addAnimation(new AlphaAnimation(preTitle, 1, 0))
                    .addAnimation(new ValueAnimation(iconOpen, iconClose) {
                        @Override
                        public void updateValue(float v) {
                            stateIcon.setSize(v);
                        }
                    })
                    .addAnimation(new ValueAnimation(spacingOpen, 0) {
                        @Override
                        public void updateValue(float v) {
                            data.setSpacing(v);
                        }
                    })
                    .addAnimation(Animation.fadeOut(actions))
                    .addAnimation(new LinearHeightAnimation(actions, buttonsOpen, 0))
                    .addAnimation(new PaddingAnimation(preContent, paddingOpen, paddingClose))
                    .setInterpolator(Interpolator.EASE_OUT);

            expand = new ParallelAnimation(300)
                    .addAnimation(new LinearHeightAnimation(this, heightClosed, heightOpen))
                    .addAnimation(new PaddingTopAnimation(this, 0, paddingTopOpen))
                    .addAnimation(new AlphaAnimation(preTitle, 0, 1))
                    .addAnimation(new ValueAnimation(iconClose, iconOpen) {
                        @Override
                        public void updateValue(float v) {
                            stateIcon.setSize(v);
                        }
                    })
                    .addAnimation(new ValueAnimation(0, spacingOpen) {
                        @Override
                        public void updateValue(float v) {
                            data.setSpacing(v);
                        }
                    })
                    .addAnimation(Animation.fadeIn(actions))
                    .addAnimation(new LinearHeightAnimation(actions, 0, buttonsOpen))
                    .addAnimation(new PaddingAnimation(preContent, paddingClose, paddingOpen))
                    .setInterpolator(Interpolator.EASE_OUT);


            Platform.runLater(() -> {
                actions.setAlpha(1f);
                setOnClickListener((e) -> {
                            if (collapsed) expand();
                            else collapse();
                        }
                );

            });
        }));

        addToContent(root);
    }

    public void collapse() {
        if (collapsed || collapse == null) return;
        collapsed = true;
        expand.stop();
        collapse.start();
    }

    public void expand() {
        if (!collapsed || expand == null) return;
        collapsed = false;
        collapse.stop();
        expand.start();
    }

    public void setBondStatus(BondState state) {
        stateLabel.setKey(state.getStatus());
        stateIcon.setImageResource(state.getIcon());
        action1.setKey(state.getAction1());
        action2.setKey(state.getAction2());

        action1.setOnClick(() -> {
            if (state.getOnAction1() != null) {
                state.getOnAction1().accept(this);
            }
        });

        action2.setOnClick(() -> {
            if (state.getOnAction2() != null) {
                state.getOnAction2().accept(this);
            }
        });

        this.bondState.set(state);
        stopLoading();
    }

    public MakeBondOverlay getMakeBondOverlay() {
        if (makeBondOverlay == null) {
            makeBondOverlay = new MakeBondOverlay(owner, this);
        }
        return makeBondOverlay;
    }

    public PendingBondsOverlay getPendingBondsOverlay() {
        if (pendingBondsOverlay == null) {
            pendingBondsOverlay = new PendingBondsOverlay(owner, this);
        }
        return pendingBondsOverlay;
    }

    private long last = -1;

    public void fetch() {
        final long command = System.currentTimeMillis();
        last = command;
        startLoading();
        Call<CheckBondResponse> call = App.api(owner).
                checkBond();

        Platform.runLater(() -> bondState.set(BondState.NO_BOND));

        Service.enqueue(call, resp -> {
            if (command != last) return;

            if (resp.body() != null) {
                other_user.set(resp.body().getUserId());
            } else {
                other_user.set("");
            }

            switch (resp.code()) {
                case 200 -> setBondStatus(BondState.BOND_ACTIVE);
                case 201 -> setBondStatus(BondState.REQUEST_SENT);
                case 204,
                     404 -> setBondStatus(BondState.NO_BOND);
                default -> ContextUtils.toast(owner, "problem_string");
            }
        });
    }

    public String getOther_user() {
        return other_user.get();
    }

    public Property<String> otherUser() {
        return other_user;
    }

    public BondState getBondState() {
        return bondState.get();
    }

    public Property<BondState> bondStateProperty() {
        return bondState;
    }

    public ColoredButton getAction2() {
        return action2;
    }
}