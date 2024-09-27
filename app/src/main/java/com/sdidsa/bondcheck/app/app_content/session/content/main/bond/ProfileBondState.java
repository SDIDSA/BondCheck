package com.sdidsa.bondcheck.app.app_content.session.content.main.bond;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class ProfileBondState extends ColoredHBox {
    private final ColoredIcon stateIcon;
    private final ColoredLabel stateLabel;
    public ProfileBondState(Context owner) {
        super(owner, Style.BACK_SEC);

        setPadding(15);
        setCornerRadius(15);

        setAlignment(Alignment.CENTER_LEFT);

        stateIcon = new ColoredIcon(owner, Style.ACCENT,
                R.drawable.empty, 48);

        ContextUtils.setMarginLeft(stateIcon, owner, 15);

        stateLabel = new ColoredLabel(owner, Style.TEXT_NORM, ""
        );
        ContextUtils.spacer(stateLabel);
        stateLabel.setFont(new Font(18));

        addViews(stateLabel, stateIcon);
    }

    public void setBondStatus(BondState state) {
        stateLabel.setKey(state.getStatus());
        stateIcon.setImageResource(state.getIcon());
    }
}
