package com.sdidsa.bondcheck.app.app_content.auth.login;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.input.InputField;
import com.sdidsa.bondcheck.abs.components.controls.input.InputUtils;
import com.sdidsa.bondcheck.abs.components.controls.input.PasswordField;
import com.sdidsa.bondcheck.abs.components.controls.scratches.ColoredSeparator;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.BondCheck;
import com.sdidsa.bondcheck.app.app_content.auth.ConnectPage;
import com.sdidsa.bondcheck.app.app_content.auth.Welcome;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.requests.UserRequest;

import java.io.IOException;

import retrofit2.Call;

public class Login extends ConnectPage {

    private final InputField username;
    private final InputField password;

    private final Button login;

    private final VBox extra;

    public Login(Context owner) {
        super(owner, "login");

        username = new InputField(owner, "username");
        username.setFont(new Font(18, FontWeight.MEDIUM));

        username.addPostIcon(R.drawable.user);

        password = new PasswordField(owner, "password");
        password.setFont(new Font(18, FontWeight.MEDIUM));

        password.addPostIcon(R.drawable.key);

        login = new ColoredButton(owner, Style.ACCENT,(s) -> Color.WHITE,
                "login");
        login.setFont(new Font(18, FontWeight.MEDIUM));

        login.setOnClick(this::login);

        Button recover = new ColoredButton(owner, (s) -> Color.TRANSPARENT,
                Style.TEXT_SEC, "forgot_pass");

        recover.setFont(new Font(17, FontWeight.MEDIUM));

        Label consent = new ColoredLabel(owner, Style.TEXT_MUT, "login_tos")
                .setFont(new Font(16, FontWeight.MEDIUM));

        root.addView(username);
        root.addView(password);
        root.addView(consent);

        VBox buttons = new VBox(owner);
        buttons.addView(login);
        buttons.addView(recover);

        root.addView(buttons);

        extra = new VBox(owner);

        extra.setSpacing(20);

        HBox sep = new HBox(owner);
        sep.setGravity(Gravity.CENTER_VERTICAL);
        sep.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        Label or = new ColoredLabel(owner, Style.TEXT_SEC, "or_you_can")
                .setFont(new Font(18));

        sep.addView(new ColoredSeparator(owner,Orientation.HORIZONTAL, 0,
                Style.TEXT_SEC));
        sep.addView(or);
        sep.addView(new ColoredSeparator(owner,Orientation.HORIZONTAL, 0,
                Style.TEXT_SEC));

        ContextUtils.setMarginHorizontal(or, owner, 15);

        extra.addView(sep);

        LoginWithButton google = new LoginWithButton(owner, "google", R.drawable.google);
        LoginWithButton facebook = new LoginWithButton(owner, "facebook", R.drawable.facebook);

        extra.addView(google);
        extra.addView(facebook);

        root.addView(extra);
    }

    private void login() {
        InputUtils.clearErrors(root);
        if(username.isEmpty()) {
            username.setError("enter_username");
            return;
        }
        if(password.isEmpty()) {
            password.setError("enter_pass");
            return;
        }
        login.startLoading();
        ContextUtils.hideKeyboard(owner);

        UserRequest user = new UserRequest(username.getValue(), "", password.getValue());
        Call<GenericResponse> call = App.api(owner).login(user);
        Service.enqueue(call, gr -> {
            login.stopLoading();

            if(gr.isSuccessful()) {
                assert gr.body() != null;
                String token = gr.body().getMessage();
                String id = gr.body().getError();
                Store.setJwtToken(token, t -> ContextUtils.setToken(owner, token));
                Store.setUserId(id, null);
                Store.setRememberUsername(username.getValue(), null);
                clearCache();
                Platform.runLater(() -> BondCheck.loadSession(owner));
            }else if(gr.code() == 500) {
                ContextUtils.toast(owner, "problem_string");
            }else {
                assert gr.errorBody() != null;
                try {
                    InputUtils.applyErrors(this, gr.errorBody().string());
                } catch (IOException e) {
                    ContextUtils.toast(owner, "problem_string");
                    ErrorHandler.handle(e, "applying errors received from server");
                }
            }
        });
    }

    @Override
    public Animation setup(int direction) {
        InputUtils.clearErrors(root);
        InputUtils.clearInputs(root);

        if(username.isEmpty()) {
            username.setValue(Store.getRememberUsername());
        }

        return super.setup(direction);
    }

    @Override
    public boolean onBack() {
        ContextUtils.loadPage(owner, Welcome.class, -1);
        return true;
    }

    private Animation hideExtra;
    private Animation showExtra;
    @Override
    public void applyInsets(Insets insets) {
        super.applyInsets(insets);
        if(insets.bottom + insets.top > ContextUtils.getScreenHeight(owner) / 4) {
            if(hideExtra == null) {
                hideExtra = new ParallelAnimation(300)
                        .addAnimations(new AlphaAnimation(extra, 0))
                        .setInterpolator(Interpolator.EASE_OUT);
            }
            hideExtra.start();
        }else {
            if(showExtra == null) {
                showExtra = new ParallelAnimation(300)
                        .addAnimations(new AlphaAnimation(extra, 1))
                        .setInterpolator(Interpolator.EASE_OUT);
            }
            showExtra.start();
        }
    }
}
