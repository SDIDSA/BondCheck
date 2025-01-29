package com.sdidsa.bondcheck.app.app_content.session.content.main;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.abs.CornerUtils;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.CreateIcon;

public class CreateMock extends VBox {
    private final HBox top, bottom;

    public CreateMock(Context owner) {
        super(owner);
        setSpacing(0);
        setAlignment(Alignment.TOP_RIGHT);

        top = new ColoredHBox(owner, Style.BACK_TER);
        top.setAlignment(Alignment.CENTER_RIGHT);
        top.setCornerRadius(CornerUtils.cornerTopRadius(owner, 20));

        ColoredLabel head = new ColoredLabel(owner, Style.TEXT_NORM, "create_post_hint");
        head.setMaxLines(1);
        head.setFont(new Font(18));
        head.setAlpha(.5f);
        head.setPadding(20);

        ColoredIcon createIcon = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.create, 56);
        createIcon.setPadding(14);
        createIcon.setAlpha(.5f);
        top.addViews(head, SpacerUtils.spacer(owner, Orientation.HORIZONTAL) ,
                createIcon);

        bottom = new ColoredHBox(owner, Style.BACK_TER);
        bottom.setAlignment(Alignment.CENTER_RIGHT);
        bottom.setCornerRadius(CornerUtils.cornerBottomRadius(owner, 20));
        bottom.setPadding(15);

        CreateIcon media = new CreateIcon(owner, R.drawable.photo_video);
        CreateIcon feeling = new CreateIcon(owner, R.drawable.smiling);
        CreateIcon location = new CreateIcon(owner, R.drawable.location_fill);

        MarginUtils.setMarginRight(feeling, owner, 10);
        MarginUtils.setMarginRight(location, owner, 10);

        bottom.addViews(location, feeling, media);

        addViews(top, bottom);
    }

    public void setRefreshProgress(float spacing) {
        setSpacing(spacing * 15);
        top.setCornerRadius(
                CornerUtils.cornerTopBottomRadius(owner, 20, spacing * 20));
        bottom.setCornerRadius(
                CornerUtils.cornerTopBottomRadius(owner, spacing * 20, 20));
    }
}
