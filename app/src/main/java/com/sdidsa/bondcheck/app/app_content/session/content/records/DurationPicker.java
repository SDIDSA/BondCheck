package com.sdidsa.bondcheck.app.app_content.session.content.records;

import android.content.Context;
import android.view.ViewGroup;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

import java.util.function.Consumer;

public class DurationPicker extends HBox {
    private Consumer<Integer> onPick;
    public DurationPicker(Context owner) {
        super(owner);
        ContextUtils.setPaddingHorizontalVertical(this, 15, 0, owner);
        setVisibility(GONE);
        DurationButton fifteen = new DurationButton(owner, 15);
        DurationButton thirty = new DurationButton(owner, 30);
        DurationButton sixty = new DurationButton(owner, 60);

        ColorIcon close = new ColoredIcon(owner, Style.TEXT_NORM, R.drawable.close, 48);
        close.setPadding(14);

        close.setOnClick(this::hide);

        fifteen.setPadding(15);
        thirty.setPadding(15);
        sixty.setPadding(15);

        ContextUtils.spacer(fifteen, Orientation.HORIZONTAL);
        ContextUtils.spacer(thirty, Orientation.HORIZONTAL);
        ContextUtils.spacer(sixty, Orientation.HORIZONTAL);

        ContextUtils.setMarginLeft(thirty, owner, 15);
        ContextUtils.setMarginLeft(sixty, owner, 15);
        ContextUtils.setMarginLeft(close, owner, 5);

        setAlpha(0);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ContextUtils.setMarginTop(this, owner, 15);

        addViews(fifteen, thirty, sixty, close);
    }

    public void setOnPick(Consumer<Integer> onPick) {
        this.onPick = onPick;
    }

    private Animation hide;
    public void hide() {
        if(hide == null) {
            hide = Animation.fadeOutDown(owner, this)
                    .setInterpolator(Interpolator.EASE_OUT)
                    .setOnFinished(() ->
                            setVisibility(GONE));
        }

        hide.start();
    }

    private Animation show;
    public void show() {
        if(show == null) {
            show = Animation.fadeInUp(owner, this)
                    .setInterpolator(Interpolator.OVERSHOOT);
        }
        setVisibility(VISIBLE);
        show.start();
    }

    private class DurationButton extends ColoredButton {
        public DurationButton(Context owner, int duration) {
            super(owner, Style.BACK_SEC, Style.TEXT_NORM, duration + "s");
            ContextUtils.spacer(this, Orientation.HORIZONTAL);
            setFont(new Font(18, FontWeight.MEDIUM));
            setPadding(15);

            setOnClick(() -> {
                if(onPick != null) {
                    onPick.accept(duration);
                }
                hide();
            });
        }
    }
}
