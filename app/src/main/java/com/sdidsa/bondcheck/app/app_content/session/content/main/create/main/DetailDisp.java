package com.sdidsa.bondcheck.app.app_content.session.content.main.create.main;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.models.PostDetail;

public class DetailDisp extends ColoredHBox {
    private final ColoredLabel desc;
    private final AppCompatTextView emoji;

    private Runnable onClick;
    private Runnable onRemove;
    public DetailDisp(Context owner) {
        super(owner, Style.BACK_PRI);
        setLayoutParams(new LayoutParams(-1, -2));
        setCornerRadius(10);
        setAlignment(Alignment.CENTER_LEFT);
        setAlpha(0f);
        MarginUtils.setMarginHorizontal(this, owner, 10);

        desc = new ColoredLabel(owner, Style.TEXT_NORM, "")
                .setFont(new Font(18));
        MarginUtils.setMarginLeft(desc, owner, 15);
        emoji = new AppCompatTextView(owner);
        MarginUtils.setMarginLeft(emoji, owner, 7);
        Label.setFont(emoji, new Font(20));

        ColoredIcon remove = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.close, 42);
        remove.setPadding(12);

        remove.setOnClick(() -> {
            if(onRemove != null) onRemove.run();
        });

        setOnClickListener(e -> {
            if(onClick != null) onClick.run();
        });

        addView(desc);
        addView(emoji);
        addView(SpacerUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(remove);
    }

    public void load(PostDetail detail) {
        desc.setKey(detail.description());
        emoji.setText(detail.emoji());
        Animation.fadeInRight(owner, this)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setOnRemove(Runnable onRemove) {
        this.onRemove = onRemove;
    }

    public Runnable getOnRemove() {
        return onRemove;
    }
}
