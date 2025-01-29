package com.sdidsa.bondcheck.app.app_content.session.content.main.create.main;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.style.Style;

public class CreateIcon extends ColoredHBox {
    public CreateIcon(Context owner) {
        this(owner, R.drawable.bond);
    }

    public CreateIcon(Context owner, @DrawableRes int res) {
        super(owner, Style.BACK_SEC);
        setCornerRadius(30);
        setPadding(13);
        setAlignment(Alignment.CENTER_LEFT);

        ColoredIcon icon = new ColoredIcon(owner, Style.TEXT_SEC, res, 26);
        addViews(icon);

    }
}
