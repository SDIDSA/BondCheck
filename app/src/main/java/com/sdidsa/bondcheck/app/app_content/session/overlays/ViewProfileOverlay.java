package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondState;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.ProfileBondState;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.Gender;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.util.ArrayList;
import java.util.Objects;

public class ViewProfileOverlay extends PartialSlideOverlay {
    private static final ArrayList<ViewProfileOverlay> cache = new ArrayList<>();
    public static ViewProfileOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        ViewProfileOverlay found = null;
        for(ViewProfileOverlay inst : cache) {
            if(!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if(found == null) {
            found = new ViewProfileOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    private final NetImage avatar;
    private final Label username;
    private final UserInfo bio;
    private final UserInfo gender;
    private final UserResponse user;

    private final ProfileBondState bondState;

    private String user_id = "";
    private ViewProfileOverlay(Context owner) {
        super(owner, 0.7);

        StackPane root = new StackPane(owner);
        root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        ColoredStackPane topBack = new ColoredStackPane(owner, Style.BACK_TER);
        topBack.setCornerRadiusTop(20);
        topBack.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ContextUtils.dipToPx(180, owner)));

        ColoredStackPane avatarBack = new ColoredStackPane(owner, Style.BACK_PRI);
        int size = ContextUtils.dipToPx(180, owner);
        avatarBack.setCornerRadius(128);
        avatarBack.setLayoutParams(new LayoutParams(size, size));
        avatarBack.setY(ContextUtils.dipToPx(40, owner));

        root.addView(topBack);
        root.addView(avatarBack);
        ContextUtils.alignInFrame(avatarBack, Alignment.TOP_CENTER);

        avatar = new NetImage(owner, Style.BACK_TER);
        avatar.setCornerRadius(160);
        avatar.setSize(160);
        avatar.setY(ContextUtils.dipToPx(50, owner));
        root.addView(avatar);
        ContextUtils.alignInFrame(avatar, Alignment.TOP_CENTER);

        VBox ui = new VBox(owner);
        ui.setPadding(15);
        ui.setSpacing(15);
        ui.setAlignment(Alignment.TOP_CENTER);

        username = new ColoredLabel(owner, Style.TEXT_NORM, "");
        username.setFont(new Font(24));
        username.centerText();

        bio = new UserInfo(owner, "user_bio");
        gender = new UserInfo(owner, "user_gender", true);

        ContextUtils.spacer(gender, Orientation.VERTICAL);

        bondState = new ProfileBondState(owner);

        ContextUtils.setMarginTop(username, owner, 220);

        ui.addView(username);
        ui.addView(bio);
        ui.addView(gender);
        ui.addViews(bondState);

        root.addView(ui);

        list.addView(root);

        user = new UserResponse();
        registerListeners();

        addOnShowing(() -> {
            user.copyFrom(new UserResponse());
            avatar.setImageResource(R.drawable.empty);
            avatar.startLoading();
            loadUser();
        });

        addOnHidden(() -> user_id = "");
    }

    public void setBondState(BondState state) {
        bondState.setBondStatus(state);
        bondState.setVisibility(state == BondState.NO_BOND ? GONE : VISIBLE);
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void show(String uid, BondState state) {
        setUser_id(uid);
        setBondState(state);
        super.show();
    }

    public void show(String uid) {
        if(uid.equals(Store.getUserId())) {
            Platform.runBack(() -> {
                Settings settings = Fragment.getInstance(owner, Settings.class);
                Platform.runLater(() -> settings.getAccountGroup().showUserProfile());
            });
        } else {
            setUser_id(uid);
            setBondState(BondState.NO_BOND);
            super.show();
        }
    }

    @Override
    public void show() {
        ErrorHandler.handle(new IllegalAccessError(
                        "can't show without setting user_id, " +
                                "use show(String) or show(String, BondState) instead"),
                "showing ViewProfileOverlay");
    }

    private void loadUser() {
        if(user_id.isEmpty()) return;
        SessionService.getUser(owner, user_id, this.user::copyFrom);
    }

    private void registerListeners() {
        user.avatar().addListener((ov, nv) -> {
            if (nv == null) {
                if (user.getGender() != null) {
                    avatar.stopLoading();
                    if (user.genderValue() == Gender.Female) {
                        avatar.setImageResource(R.drawable.avatar_female);
                    } else {
                        avatar.setImageResource(R.drawable.avatar_male);
                    }
                }
            } else {
                Platform.runLater(() -> ImageProxy.getImage(owner, nv, bmp -> {
                    avatar.stopLoading();
                    avatar.setImageBitmap(bmp);
                }));
            }
        });

        user.gender().addListener((ov, nv) -> {
            if(nv != null) {
                gender.setValue(Gender.valueOf(nv).getDisplay());
            }
            if (nv != null && user.getAvatar() == null) {
                avatar.stopLoading();
                if (user.genderValue() == Gender.Female) {
                    avatar.setImageResource(R.drawable.avatar_female);
                } else {
                    avatar.setImageResource(R.drawable.avatar_male);
                }
            }
        });

        user.username().addListener((ov, nv) ->
                username.setText(Objects
                .requireNonNullElse(nv, "")));

        user.bio().addListener((ov, nv) ->
                bio.setValue(Objects
                .requireNonNullElse(nv, "no bio yet...")));
    }

    private static class UserInfo extends HBox {
        private final ColoredLabel value;

        private final boolean keyed;
        public UserInfo(Context owner, String titleString) {
            this(owner, titleString, false);
        }
        public UserInfo(Context owner, String titleString, boolean keyed) {
            super(owner);
            this.keyed = keyed;
            setCornerRadius(15);
            setPadding(5);
            setAlignment(Alignment.TOP_LEFT);

            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            ColoredLabel title = new ColoredLabel(owner, Style.TEXT_SEC, titleString);
            title.setLines(1);
            title.setFont(new Font(18, FontWeight.MEDIUM));

            value = new ColoredLabel(owner, Style.TEXT_NORM, "");
            value.setFont(new Font(18));

            VBox labels = new VBox(owner);
            labels.addViews(title, value);
            labels.setSpacing(5);
            ContextUtils.spacer(labels);
            addViews(labels);
        }

        public void setValue(String value) {
            Platform.runLater(() -> {
                if(keyed) {
                    this.value.setKey(value);
                }else {
                    this.value.setText(value);
                }
            });
        }
    }
}
