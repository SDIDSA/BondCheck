package com.sdidsa.bondcheck.app.app_content.session.content.main.create.main;

import android.content.Context;
import android.text.TextUtils;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.location.AddressProxy;
import com.sdidsa.bondcheck.abs.components.controls.scratches.ColoredSeparator;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Separator;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.location.LocationDetail;

public class LocationDisp extends ColoredHBox implements Localized {
    private final ColoredLabel desc;
    private final ColoredIcon typeIcon;
    private final ColoredIcon remove;
    private Runnable onClick;
    private Runnable onRemove;

    private LocationDetail data;

    public LocationDisp(Context owner) {
        super(owner, Style.BACK_PRI);
        setLayoutParams(new LayoutParams(-1, SizeUtils.dipToPx(42, owner)));
        setCornerRadius(10);
        setAlignment(Alignment.CENTER_LEFT);
        setAlpha(0f);
        MarginUtils.setMarginHorizontal(this, owner, 10);

        desc = new ColoredLabel(owner, Style.TEXT_NORM, "")
                .setFont(new Font(18));
        desc.setEllipsize(TextUtils.TruncateAt.END);
        SpacerUtils.spacer(desc, -2, -2);
        MarginUtils.setMarginHorizontal(desc, owner, 15);

        typeIcon = new ColoredIcon(owner, Style.TEXT_SEC,
                R.drawable.empty, 28);
        typeIcon.setPadding(5);
        MarginUtils.setMarginHorizontal(typeIcon, owner, 5);

        Separator s = new ColoredSeparator(owner, Orientation.VERTICAL, 10, Style.TEXT_SEC);
        s.setAlpha(.3f);
        s.setThickness(2);

        remove = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.close, 42);
        remove.setPadding(12);

        remove.setOnClick(() -> {
            if(onRemove != null) onRemove.run();
        });

        setOnClickListener(e -> {
            if(onClick != null) onClick.run();
        });

        addView(typeIcon);
        addView(s);
        addView(desc);

        applyLocale(LocaleUtils.getLocale(owner));
    }

    public void load(LocationDetail location) {
        this.data = location;
        typeIcon.setImageResource(location.type().getIcon());
        Animation.fadeInRight(owner, this)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
        applyLocale(LocaleUtils.getLocale(owner).get());
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setOnRemove(Runnable onRemove) {
        this.onRemove = onRemove;
        addView(remove);
    }

    public Runnable getOnRemove() {
        return onRemove;
    }

    @Override
    public void applyLocale(Locale locale) {
        if(data == null) return;
        desc.setText("");
        AddressProxy.getAddress(data.getLocation(), locale.getLang(), desc::setText);
    }
}
