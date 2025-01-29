package com.sdidsa.bondcheck.app.app_content.session.content.main;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.style.Style;

public class Content extends ColoredStackPane {
    public Content(Context owner) {
        super(owner, Style.BACK_TER);
        setCornerRadius(20);
        setPadding(40);

        setAlpha(.5f);

        addAligned(new ColoredLabel(owner, Style.TEXT_SEC, "Content")
                .setFont(new Font(18, FontWeight.MEDIUM)), Alignment.CENTER);
    }
}
