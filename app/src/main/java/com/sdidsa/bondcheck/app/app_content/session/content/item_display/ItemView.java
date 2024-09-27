package com.sdidsa.bondcheck.app.app_content.session.content.item_display;

import android.content.Context;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredDateLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.DateFormat;
import com.sdidsa.bondcheck.abs.components.controls.text.DateLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class ItemView extends ColoredHBox {
    private final DateLabel time;
    protected final Label second;

    protected Item item;

    protected ItemView(Context owner) {
        super(owner, Style.BACK_TER);

        setPadding(15);
        setCornerRadius(15);

        VBox info = new VBox(owner);
        info.setSpacing(15);

        time = new ColoredDateLabel(owner, Style.TEXT_NORM);
        time.setFont(new Font(18, FontWeight.MEDIUM));

        second = new ColoredLabel(owner, Style.TEXT_NORM, "");
        second.setFont(new Font(18, FontWeight.MEDIUM));

        info.addViews(time, second);
        ContextUtils.spacer(info);
        addViews(info);
    }

    protected void loadItem(Item item) {
        this.item = item;
        setTranslationX(0);

        time.setFormat(DateFormat.RELATIVE);
        time.setDate(item.created_at());

        second.setText("");
    }

    private Animation show;
    public Animation show() {
        if(show == null) {
            show = Animation.fadeInUp(owner, this);
        }
        show.setOnFinished(null);
        setAlpha(0);
        return show;
    }

    private Animation showAlone;
    public Animation showAlone() {
        if(showAlone == null) {
            showAlone = Animation.fadeInRight(owner, this)
                    .setInterpolator(Interpolator.OVERSHOOT);
        }
        showAlone.setOnFinished(null);
        setAlpha(0);
        return showAlone;
    }

    private Animation hide;
    public Animation hide() {
        if(hide == null) {
            hide = Animation.fadeOutRight(owner, this)
                    .setInterpolator(Interpolator.OVERSHOOT);
        }
        hide.setOnFinished(null);
        return hide;
    }
}
