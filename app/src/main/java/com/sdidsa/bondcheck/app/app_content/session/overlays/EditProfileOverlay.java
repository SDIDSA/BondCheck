package com.sdidsa.bondcheck.app.app_content.session.overlays;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.MultipleOptionOverlay;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.data.observable.ChangeListener;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.AlignUtils;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.Gender;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class EditProfileOverlay extends PartialSlideOverlay {

    private final NetImage avatar;
    private final UserInfo username;
    private final UserInfo bio;
    private final UserInfo gender;

    private UserResponse user;

    private InputSlideOverlay usernameOverlay;
    private InputSlideOverlay bioOverlay;
    private MultipleOptionOverlay genderOverlay;

    private final ChangeListener<String> avatarListener;
    private final ChangeListener<String> genderListener;
    private final ChangeListener<String> usernameListener;
    private final ChangeListener<String> bioListener;

    public EditProfileOverlay(Context owner) {
        super(owner, 0.7);

        StackPane root = new StackPane(owner);

        ColoredStackPane topBack = new ColoredStackPane(owner, Style.BACK_TER);
        topBack.setCornerRadiusTop(20);
        topBack.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, SizeUtils.dipToPx(180, owner)));

        ColoredStackPane avatarBack = new ColoredStackPane(owner, Style.BACK_PRI);
        int size = SizeUtils.dipToPx(180, owner);
        avatarBack.setCornerRadius(128);
        avatarBack.setLayoutParams(new LayoutParams(size, size));
        avatarBack.setY(SizeUtils.dipToPx(40, owner));

        root.addView(topBack);
        root.addView(avatarBack);

        AlignUtils.alignInFrame(avatarBack, Alignment.TOP_CENTER);

        int avatarSize = SizeUtils.dipToPx(160, owner);
        avatar = new NetImage(owner, Style.BACK_TER);
        avatar.setCornerRadius(160);
        avatar.setSize(160);
        avatar.setY(SizeUtils.dipToPx(50, owner));
        root.addView(avatar);
        AlignUtils.alignInFrame(avatar, Alignment.TOP_CENTER);

        VBox ui = new VBox(owner);
        ui.setPadding(15);
        ui.setSpacing(15);

        ColoredIcon editPfp = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.camera, 42);
        editPfp.setPadding(5);

        username = new UserInfo(owner, "username");
        bio = new UserInfo(owner, "user_bio");
        gender = new UserInfo(owner, "user_gender", true);

        avatar.startLoading();

        HBox top = new HBox(owner);
        PaddingUtils.setPaddingTop(top, 110, owner);

        top.setAlignment(Alignment.CENTER_RIGHT);
        MarginUtils.setMarginTop(username, owner, 64);


        top.addView(editPfp);
        ui.addView(top);
        ui.addView(username);
        ui.addView(bio);
        ui.addView(gender);

        root.addView(ui);

        list.addView(root);

        avatarListener = (ov, nv) -> {
            if (nv == null) {
                if (user.getGender() != null) {
                    if (user.genderValue() == Gender.Female) {
                        avatar.setImageResource(R.drawable.avatar_female);
                    } else {
                        avatar.setImageResource(R.drawable.avatar_male);
                    }
                }
            } else {
                Platform.runLater(() ->
                        ImageProxy.getImageThumb(owner, nv, avatarSize, avatar::setImageBitmap));
            }
        };

        genderListener = (ov, nv) -> {
            gender.setValue(user.genderValue().getDisplay());
            if (nv != null && user.getAvatar() == null) {
                if (user.genderValue() == Gender.Female) {
                    avatar.setImageResource(R.drawable.avatar_female);
                } else {
                    avatar.setImageResource(R.drawable.avatar_male);
                }
            }
        };

        usernameListener = (ov, nv) -> {
            if (nv != null) {
                username.setValue(nv);
            }
        };

        bioListener = (ov, nv) -> {
            if (nv != null && !nv.isBlank()) {
                bio.setValue(nv);
            } else {
                bio.setValue("no bio yet...");
            }
        };

        registerListeners();

        editPfp.setOnClick(this::editPfp);

        username.setOnEdit(() -> {
            if (usernameOverlay == null) {
                usernameOverlay = getUsernameOverlay(owner);
            }
            usernameOverlay.show();
        });

        bio.setOnEdit(() -> {
            if (bioOverlay == null) {
                bioOverlay = getBioOverlay(owner);
            }
            bioOverlay.show();
        });

        gender.setOnEdit(() -> {
            if (genderOverlay == null) {
                genderOverlay = getGenderOverlay(owner);
            }
            genderOverlay.show();
        });

        MultipleOptionOverlay genderOverlay = getGenderOverlay(owner);
        gender.setOnEdit(genderOverlay::show);
    }

    private void editPfp() {
        ContextUtils.pickImage(owner, image -> {
            avatar.startLoading();
            image.getThumbnail(owner, 512, bmp -> {
                File file = ImageProxy.saveTemp(bmp);
                Call<GenericResponse> call = App.api(owner)
                        .setAvatar(MultipartBody.Part.createFormData(
                                "avatar",
                                file.getName(),
                                RequestBody.create(Objects.requireNonNull(MediaType.parse("image/png")),
                                        file)));

                Service.enqueue(call, resp -> {
                    if (resp.isSuccessful()) {
                        ContextUtils.toast(owner, "avatar_changed");
                        SessionService.getUser(owner, user.getId(), u -> {
                           //ignore
                        }, false);
                    } else {
                        ContextUtils.toast(owner, "problem_string");
                    }
                });
            }, () -> ContextUtils.toast(owner, "problem_string"));
        });
    }

    private @NonNull InputSlideOverlay getUsernameOverlay(Context owner) {
        InputSlideOverlay overlay = new InputSlideOverlay(owner, "change_username");

        overlay.addOnShowing(() -> {
            overlay.setValue(user.getUsername());
            overlay.enableAction(false);
        });

        overlay.valueProperty().addListener((ov, nv) ->
                overlay.enableAction(!nv.trim().equals(user.getUsername().trim())));

        overlay.setOnSave(res -> {
            overlay.startLoading();

            Call<GenericResponse> call = App.api(owner)
                    .setUsername(new StringRequest(res.trim()));

            Service.enqueue(call, resp -> {
                overlay.stopLoading();
                if (resp.isSuccessful()) {
                    overlay.hide();
                    ContextUtils.toast(owner, "username_changed");
                    user.setUsername(res);
                } else {
                    switch (resp.code()) {
                        case 401 -> ContextUtils.toast(owner, "Invalid format");
                        case 402 -> ContextUtils.toast(owner, "Parameters Required");
                        case 403 -> ContextUtils.toast(owner, "This username is taken");
                        default -> ContextUtils.toast(owner, "Internal Server Error");
                    }
                }
            });
        });
        return overlay;
    }

    private @NonNull InputSlideOverlay getBioOverlay(Context owner) {
        InputSlideOverlay overlay = new InputSlideOverlay(owner, "change_bio");

        overlay.setMultiLine(3);
        overlay.addOnShowing(() -> {
            overlay.setValue(user.getBio());
            overlay.enableAction(false);
        });

        overlay.valueProperty().addListener((ov, nv) ->
                overlay.enableAction((user.getBio() == null && !nv.trim().isEmpty()) ||
                        (user.getBio() != null && !nv.trim().equals(user.getBio().trim()))));

        overlay.setOnSave(res -> {
            overlay.startLoading();

            Call<GenericResponse> call = App.api(owner)
                    .setBio(new StringRequest(res.trim()));

            Service.enqueue(call, resp -> {
                overlay.stopLoading();
                if (resp.isSuccessful()) {
                    overlay.hide();
                    ContextUtils.toast(owner, "bio_changed");
                    user.setBio(res);
                } else {
                    ContextUtils.toast(owner, "problem_string");
                }
            });
        });
        return overlay;
    }

    private MultipleOptionOverlay getGenderOverlay(Context owner) {
        MultipleOptionOverlay res = new MultipleOptionOverlay(owner, "set_gender",
                (s) -> user!= null && s.equals(user.genderValue().getDisplay()));
        for (Gender gender : Gender.values()) {
            if(gender == Gender.Unknown) continue;
            res.addButton(gender.getDisplay(), () -> {
                res.startLoading(gender.getDisplay());

                Call<GenericResponse> call = App.api(owner)
                        .setGender(new StringRequest(gender.name()));

                Service.enqueue(call, resp -> {
                    res.stopLoading(gender.getDisplay());
                    res.hide();
                    if (resp.isSuccessful()) {
                        ContextUtils.toast(owner, "gender_changed");
                        user.setGender(gender.name());
                    } else {
                        ContextUtils.toast(owner, "problem_string");
                    }
                });
            });
        }

        return res;
    }

    private void registerListeners() {
        AtomicBoolean hidden = new AtomicBoolean(false);
        addOnShowing(() -> {
            hidden.set(false);
            SessionService.getUser(owner, Store.getUserId(), user -> {
                if(hidden.get()) return;
                this.user = user;
                user.avatar().addListener(avatarListener);
                user.gender().addListener(genderListener);
                user.username().addListener(usernameListener);
                user.bio().addListener(bioListener);
            }, false);
        });

        addOnHidden(() -> {
            hidden.set(true);
            if(user == null) return;
            this.user.avatar().removeListener(avatarListener);
            this.user.gender().removeListener(genderListener);
            this.user.username().removeListener(usernameListener);
            this.user.bio().removeListener(bioListener);
        });
    }

    @Override
    public void applySystemInsets(Insets insets) {
        if(insets.top + insets.bottom <= ContextUtils.getScreenHeight(owner) / 4f) {
            super.applySystemInsets(insets);
        }
    }

    private static class UserInfo extends HBox {
        private final ColoredLabel value;
        private final ColorIcon edit;

        private final boolean keyed;
        public UserInfo(Context owner, String titleString) {
            this(owner, titleString, false);
        }
        public UserInfo(Context owner, String titleString, boolean keyed) {
            super(owner);
            this.keyed = keyed;
            setCornerRadius(15);
            setPadding(5);
            setAlignment(Alignment.CENTER);

            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            ColoredLabel title = new ColoredLabel(owner, Style.TEXT_SEC, titleString);
            title.setLines(1);
            title.setFont(new Font(18, FontWeight.MEDIUM));

            value = new ColoredLabel(owner, Style.TEXT_NORM, "");
            value.setFont(new Font(18));

            VBox labels = new VBox(owner);
            labels.addViews(title, value);
            labels.setSpacing(5);
            SpacerUtils.spacer(labels);

            edit = new ColoredIcon(owner, Style.TEXT_SEC, Style.BACK_SEC, R.drawable.edit, 40);
            edit.setRadiusNoClip(10);
            edit.setPadding(10);
            addViews(labels, edit);
        }

        public void setOnEdit(Runnable action) {
            edit.setOnClick(action);
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
