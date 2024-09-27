package com.sdidsa.bondcheck.app.app_content.session.content.main.bond;

import android.content.Context;
import android.graphics.Color;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.overlays.ViewProfileOverlay;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.Gender;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.util.ArrayList;

import retrofit2.Call;

public class UserCard extends HBox implements Styleable {
    private static final ArrayList<UserCard> cache = new ArrayList<>();

    public synchronized static UserCard make(Context owner, BondStatus source, UserResponse user,
                                UserCardMode mode) {
        cache.removeIf(item -> item.getOwner() != owner);

        UserCard card = null;
        for(UserCard c : cache) {
            if(c.getParent() == null) {
                card = c;
                break;
            }
        }
        if(card == null) {
            card = new UserCard(owner, source);
            cache.add(card);
        }

        card.loadUser(user, mode);

        return card;
    }

    private static final float SIZE = 56;
    private final NetImage avatar;
    private final Label username;
    private final ColorIcon gender;
    private final Button action;
    private final BondStatus source;

    public UserCard(Context owner) {
        this(owner, null);
    }

    public UserCard(Context owner, BondStatus source) {
        super(owner);
        setAlignment(Alignment.CENTER_LEFT);
        this.source = source;

        avatar = new NetImage(owner);
        avatar.setSize(SIZE);
        avatar.setCornerRadius(SIZE);

        VBox info = new VBox(owner);
        info.setSpacing(5);
        username = new Label(owner, "");
        username.setFont(new Font(18));

        gender = new ColorIcon(owner, -1, 18);

        action = new Button(owner, "");
        action.setPadding(10);
        action.getLayoutParams().width = ContextUtils.dipToPx(100, owner);

        info.addViews(username, gender);

        ContextUtils.setMarginLeft(info, owner, 15);
        addViews(avatar, info, ContextUtils.spacer(owner, Orientation.HORIZONTAL), action);

        applyStyle(ContextUtils.getStyle(owner));
    }

    private UserResponse user;
    private void loadUser(UserResponse user, UserCardMode mode) {
        this.user = user;
        avatar.startLoading();
        if(user.getAvatar() != null) {
            ImageProxy.getImage(owner, user.getAvatar(),
                    avatar::setImageBitmap);
        }else if(user.genderValue() == Gender.Female){
            avatar.setImageResource(R.drawable.avatar_female);
        }else {
            avatar.setImageResource(R.drawable.avatar_male);
        }

        username.setText(user.getUsername());
        switch (user.genderValue()) {
            case Male -> gender.setImageResource(R.drawable.male);
            case Female -> gender.setImageResource(R.drawable.female);
            default -> gender.setImageResource(-1);
        }

        switch (mode) {
            case SEND_MODE -> sendRequest();
            case PENDING_MODE -> acceptRequest();
        }
    }

    private void sendRequest() {
        Context owner = source.getOwner();

        setOnClickListener((e) ->
                ViewProfileOverlay.getInstance(owner)
                        .show(user.getId(), BondState.NO_BOND));

        action.setKey("send_request");

        action.setOnClick(() -> {
            ContextUtils.hideKeyboard(owner);
            action.startLoading();
            Call<GenericResponse> call = App.api(owner)
                    .sendRequest(new StringRequest(String.valueOf(user.getId())));

            Service.enqueue(call, resp -> {
                action.stopLoading();
                switch(resp.code()) {
                    case 201 -> {
                        source.getMakeBondOverlay().addOnHiddenOnce(() -> {
                            ContextUtils.toast(owner, "Request sent");
                            source.fetch();
                        });
                        source.getMakeBondOverlay().hide();
                    }
                    case 400 -> ContextUtils.toast(owner, "This user is taken");
                    case 430 -> {
                        ((VBox) getParent()).removeView(this);
                        ContextUtils.toast(owner, "User not found");
                    }
                    case 431 -> {
                        ContextUtils.toast(owner, "You have a pending request from this user");
                        source.getMakeBondOverlay().hide();
                        source.getPendingBondsOverlay().show();
                    }
                    default -> ContextUtils.toast(owner, "problem_string");
                }
            });
        });
    }

    private void acceptRequest() {
        Context owner = source.getOwner();

        setOnClickListener((e) ->
                ViewProfileOverlay.getInstance(owner)
                        .show(user.getId(), BondState.RECEIVED));

        action.setKey("Accept");
        action.setOnClick(() -> {
            action.startLoading();

            Call<GenericResponse> call = App.api(owner)
                    .acceptBond(new StringRequest(String.valueOf(user.getId())));

            Service.enqueue(call, resp -> {
                action.stopLoading();
                switch (resp.code()) {
                    case 200 -> {
                        source.getPendingBondsOverlay().addOnHiddenOnce(() -> {
                            ContextUtils.toast(owner, "Bond accepted");
                            source.fetch();
                        });
                        source.getPendingBondsOverlay().hide();
                    }
                    case 430 -> {
                        source.getPendingBondsOverlay().removeUser(this);
                        ContextUtils.toast(owner, "Request not found");
                    }
                    default -> ContextUtils.toast(owner, "problem_string");
                }
            });
        });
    }

    @Override
    public void applyStyle(Style style) {
        username.setFill(style.getTextSecondary());
        gender.setFill(style.getTextSecondary());
        action.setFill(style.getAccent());
        action.setTextFill(Color.WHITE);
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
