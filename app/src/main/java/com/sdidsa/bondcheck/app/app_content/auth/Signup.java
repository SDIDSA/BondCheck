package com.sdidsa.bondcheck.app.app_content.auth;

import android.content.Context;
import android.graphics.Color;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.input.InputField;
import com.sdidsa.bondcheck.abs.components.controls.input.InputUtils;
import com.sdidsa.bondcheck.abs.components.controls.input.PasswordField;
import com.sdidsa.bondcheck.abs.components.controls.input.checkBox.LabeledCheckBox;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.auth.login.Login;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.requests.UserRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class Signup extends ConnectPage {
    private final InputField email;
    private final InputField username;
    private final InputField password;

    private final Button next;

    public Signup(Context owner) {
        super(owner, "signup");

        username = new InputField(owner, "username");
        username.setFont(new Font(18, FontWeight.MEDIUM));

        username.addPostIcon(R.drawable.user);

        email = new InputField(owner, "email");
        email.setFont(new Font(18, FontWeight.MEDIUM));

        email.addPostIcon(R.drawable.at);

        password = new PasswordField(owner, "password");
        password.setFont(new Font(18, FontWeight.MEDIUM));

        password.addPostIcon(R.drawable.key);

        LabeledCheckBox consent = new LabeledCheckBox(owner, "tos");
        consent.getLabel().addParam(0, ContextUtils.getAppName(owner));
        consent.setFont(new Font(17, FontWeight.MEDIUM));
        consent.setLineSpacing(4);
        consent.setCheckSize(36);
        consent.setSpacing(15);

        next = new ColoredButton(owner, Style.ACCENT, (s) -> Color.WHITE, "signup");
        next.setFont(new Font(17, FontWeight.MEDIUM));

        next.setOnClick(this::signup);
        consent.checkedProperty().addListener((ov, nv) -> next.setEnabled(nv));

        root.addViews(username, email, password, consent, next, SpacerUtils.spacer(owner, Orientation.VERTICAL));
    }

    @Override
    public Animation setup(int direction) {
        InputUtils.clearErrors(root);
        InputUtils.clearInputs(root);
        return super.setup(direction);
    }

    private void signup() {
        InputUtils.clearErrors(root);
        if (username.isEmpty()) {
            username.setError("username_required");
            return;
        }
        if (email.isEmpty()) {
            email.setError("enter_email");
            return;
        }
        if (password.isEmpty()) {
            password.setError("pass_required");
            return;
        }
        next.startLoading();
        ContextUtils.hideKeyboard(owner);

        UserRequest user = new UserRequest(username.getValue(), email.getValue(),
                password.getValue());
        Call<GenericResponse> call = App.api(owner).register(user);

        Service.enqueue(call, gr -> {
            next.stopLoading();
            if (gr.isSuccessful()) {
                ContextUtils.toast(owner, "signup_success", 4000);
                Store.setRememberUsername(username.getValue(), null);
                ContextUtils.loadPage(owner, Login.class, -1);
            } else if (gr.code() == 500) {
                ContextUtils.toast(owner, "problem_string");
            } else {
                try (ResponseBody erB = gr.errorBody()){
                    assert erB != null;
                    InputUtils.applyErrors(this, erB.string());
                } catch (IOException e) {
                    ContextUtils.toast(owner, "problem_string");
                    ErrorHandler.handle(e, "applying errors returned from server");
                }
            }
        });
    }

    @Override
    public boolean onBack() {
        ContextUtils.loadPage(owner, Welcome.class, -1);
        return true;
    }
}
