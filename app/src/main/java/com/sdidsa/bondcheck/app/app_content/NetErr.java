package com.sdidsa.bondcheck.app.app_content;

import android.content.Context;

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
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class NetErr extends Page {

    public NetErr(Context owner) {
        super(owner);

        VBox root = new VBox(owner);
        root.setSpacing(50);
        root.setAlignment(Alignment.TOP_CENTER);
        root.setPadding(20);

        HBox top = new HBox(owner);
        top.setAlignment(Alignment.CENTER);
        ColoredIcon heart = new ColoredIcon(owner, Style.ACCENT, R.drawable.heart_broken, 72);
        ColoredLabel name = new ColoredLabel(owner, Style.ACCENT, "").setFont(new Font(42, FontWeight.BOLD));
        name.setText(ContextUtils.getAppName(owner));
        top.addViews(heart, name);

        ContextUtils.setMarginLeft(name, owner, 20);

        ColoredLabel rest = new ColoredLabel(owner,
                Style.TEXT_NORM, "Failed to connect to the servers..."
        )
                .setFont(new Font(20, FontWeight.MEDIUM));
        rest.centerText();

        Button retry = new ColoredButton(owner,
                Style.BACK_TER,
                Style.TEXT_SEC, "Retry");
        retry.setFont(new Font(20, FontWeight.MEDIUM));
        retry.setElevation(0);

        retry.setOnClick(() -> ContextUtils.postCreate(owner));

        VBox buttons = new VBox(owner);
        buttons.setSpacing(10);
        buttons.addViews(retry);

        root.addViews(ContextUtils.spacer(owner, Orientation.VERTICAL), top, rest, buttons, ContextUtils.spacer(owner, Orientation.VERTICAL));

        addCentered(root);
    }

    @Override
    public Animation setup(int direction) {
        if(setup == null) {
            setup = new ParallelAnimation(300)
                    .addAnimation(Animation.fadeInScaleUp(this))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        setAlpha(0);
        return setup;
    }

    @Override
    public Animation destroy(int direction) {
        if(destroy == null) {
            destroy = new ParallelAnimation(300)
                    .addAnimation(Animation.fadeOutScaleDown(this))
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
