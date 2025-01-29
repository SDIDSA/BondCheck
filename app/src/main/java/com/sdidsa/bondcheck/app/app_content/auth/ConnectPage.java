package com.sdidsa.bondcheck.app.app_content.auth;

import android.content.Context;
import android.view.Gravity;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.padding.PaddingAnimation;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;

public abstract class ConnectPage extends Page {
    protected final VBox root;
    public ConnectPage(Context owner, String title) {
        super(owner);

        root = new VBox(owner);
        root.setPadding(20);
        root.setSpacing(20);

        root.setGravity(Gravity.TOP);

        Label hello = new ColoredLabel(owner,
                Style.TEXT_NORM, title
        )
                .setFont(new Font(24, FontWeight.SEMIBOLD));

        HBox preHello = new HBox(owner);
        preHello.setGravity(Gravity.CENTER_VERTICAL);

        ColoredIcon back = new ColoredIcon(owner, Style.TEXT_NORM, R.drawable.arrow_left, 48);
        back.setPadding(10);
        back.setAutoMirror(true);
        back.setContentDescription("Go Back to welcome page");
        MarginUtils.setMarginRight(back, owner, 15);

        back.setImagePadding(3);
        back.setOnClick(this::onBack);

        preHello.addView(back);
        preHello.addView(hello);

        root.addView(preHello);

        addView(root);
    }

    @Override
    public Animation setup(int direction) {
        if(setup == null) {
            setup = new ParallelAnimation(400)
                    .addAnimation(direction > 0 ? Animation.fadeInScaleUp(this):
                            Animation.fadeInScaleDown(this))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        setAlpha(0);
        return setup;
    }

    @Override
    public Animation destroy(int direction) {
        if(destroy == null) {
            destroy = new ParallelAnimation(400)
                    .addAnimation(direction > 0 ? Animation.fadeOutScaleUp(this): Animation.fadeOutScaleDown(this))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        return destroy;
    }

    @Override
    public void applyInsets(Insets insets) {
        new PaddingAnimation(250, this, PaddingUtils.getPadding(this), insets)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }
}
