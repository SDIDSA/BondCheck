package com.sdidsa.bondcheck.app.app_content.session.content.history;

import android.content.Context;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.shared.HomeSection;

public class HistoryThumbnail extends ColoredStackPane {
    public HistoryThumbnail(Context owner) {
        super(owner, Style.BACK_SEC);
        int size = SizeUtils.dipToPx(HomeSection.ITEM_SIZE, owner);
        setLayoutParams(new LinearLayout.LayoutParams(size,size));
        setCornerRadius(15);

        setElevation(SizeUtils.dipToPx(5, owner));
    }
}
