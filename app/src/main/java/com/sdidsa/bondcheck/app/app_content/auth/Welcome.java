package com.sdidsa.bondcheck.app.app_content.auth;

import android.content.Context;
import android.graphics.Color;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.auth.login.Login;

public class Welcome extends Page {

    private final HBox top;
    private final ColoredLabel rest;
    private final VBox buttons;

    public Welcome(Context owner) {
        super(owner);

        VBox root = new VBox(owner);
        root.setSpacing(50);
        root.setAlignment(Alignment.TOP_CENTER);
        root.setPadding(20);

        top = new HBox(owner);
        top.setAlignment(Alignment.CENTER);
        ColoredIcon heart = new ColoredIcon(owner, Style.ACCENT, R.drawable.heart, 80);
        ColoredLabel name = new ColoredLabel(owner, Style.ACCENT, "")
                .setFont(new Font(42, FontWeight.BOLD));
        name.setText(ContextUtils.getAppName(owner));
        MarginUtils.setMarginRight(heart, owner, 20);
        top.addViews(heart, name);

        rest = new ColoredLabel(owner, Style.TEXT_NORM, "welcome_header")
                .setFont(new Font(20, FontWeight.MEDIUM));
        rest.centerText();

        Button login = new ColoredButton(owner, Style.ACCENT,(s) -> Color.WHITE, "login");
        login.setFont(new Font(18, FontWeight.MEDIUM));
        Button signup = new ColoredButton(owner, s -> Color.TRANSPARENT, Style.ACCENT, "signup");
        signup.setFont(new Font(18, FontWeight.MEDIUM));

        login.setOnClick(() -> ContextUtils.loadPage(owner, Login.class, 1));

        signup.setOnClick(() -> ContextUtils.loadPage(owner, Signup.class, 1));

        buttons = new VBox(owner);
        buttons.setSpacing(10);
        buttons.addViews(login, signup);

        root.addViews(SpacerUtils.spacer(owner, Orientation.VERTICAL),top, rest, SpacerUtils.spacer(owner, Orientation.VERTICAL), buttons);

        addCentered(root);
    }

    @Override
    public Animation setup(int direction) {
        if(setup == null) {
            setup = new ParallelAnimation(400)
                    .addAnimation(Animation.fadeInDown(owner, top))
                    .addAnimation(Animation.fadeInScaleDown(rest))
                    .addAnimation(Animation.fadeInUp(owner, buttons))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        top.setAlpha(0);
        rest.setAlpha(0);
        buttons.setAlpha(0);
        return setup;
    }

    @Override
    public Animation destroy(int direction) {
        if(destroy == null) {
            destroy = new ParallelAnimation(400)
                    .addAnimation(Animation.fadeOutUp(owner, top))
                    .addAnimation(Animation.fadeOutScaleUp(rest))
                    .addAnimation(Animation.fadeOutDown(owner, buttons))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        return destroy;
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public void applyInsets(Insets insets) {
        setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }
}
