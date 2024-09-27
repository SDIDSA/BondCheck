package com.sdidsa.bondcheck.app.app_content.session.content.main.bond;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.main.shared.HomeSection;
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

    public BondStatus(Context owner) {
        super(owner, "bond_status", R.drawable.heart);

        HBox root = new HBox(owner);
        root.setPadding(5);
        root.setAlignment(Alignment.CENTER);

        bondState = new Property<>(BondState.NO_BOND);
        other_user = new Property<>("");

        stateIcon = new ColoredIcon(owner, Style.ACCENT,
                R.drawable.empty, 72);

        ContextUtils.setMarginLeft(stateIcon, owner, 15);

        stateLabel = new ColoredLabel(owner, Style.TEXT_NORM, ""
        );
        stateLabel.setLines(2);
        stateLabel.setFont(new Font(18));

        action1 = new ColoredButton(owner, Style.BACK_SEC
                , Style.TEXT_NORM
                , "");
        action1.setPadding(8);

        action2 = new ColoredButton(owner, Style.BACK_SEC
                , Style.TEXT_NORM
                , "");
        action2.setPadding(8);

        ContextUtils.spacer(action1);
        ContextUtils.spacer(action2);
        ContextUtils.setMarginLeft(action2, owner, 10);

        action1.setFont(new Font(16));
        action2.setFont(new Font(16));

        VBox data = new VBox(owner);
        data.setSpacing(10);
        data.setAlignment(Alignment.CENTER_LEFT);


        HBox actions = new HBox(owner);
        actions.addViews(action1, action2);

        data.addView(stateLabel);
        data.addView(actions);

        ContextUtils.spacer(data);

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

        addToContent(root);
    }

    public void setBondStatus(BondState state) {
        stateLabel.setKey(state.getStatus());
        stateIcon.setImageResource(state.getIcon());
        action1.setKey(state.getAction1());
        action2.setKey(state.getAction2());

        action1.setOnClick(() -> {
            if(state.getOnAction1() != null) {
                state.getOnAction1().accept(this);
            }
        });

        action2.setOnClick(() -> {
            if(state.getOnAction2() != null) {
                state.getOnAction2().accept(this);
            }
        });

        this.bondState.set(state);
        stopLoading();
    }

    public MakeBondOverlay getMakeBondOverlay() {
        if(makeBondOverlay == null) {
            makeBondOverlay = new MakeBondOverlay(owner, this);
        }
        return makeBondOverlay;
    }

    public PendingBondsOverlay getPendingBondsOverlay() {
        if(pendingBondsOverlay == null) {
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
            if(command != last) return;

            if(resp.body() != null) {
                other_user.set(resp.body().getUserId());
            }else {
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