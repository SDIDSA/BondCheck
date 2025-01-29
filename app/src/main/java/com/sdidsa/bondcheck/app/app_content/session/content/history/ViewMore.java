package com.sdidsa.bondcheck.app.app_content.session.content.history;

import android.content.Context;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.shared.HomeSection;

public class ViewMore extends VBox {
    public ViewMore(Context owner) {
        super(owner);
        int size = SizeUtils.dipToPx(HomeSection.ITEM_SIZE, owner);
        setLayoutParams(new LinearLayout.LayoutParams(size,size));

        setAlignment(Alignment.CENTER);
        setSpacing(15);

        ColoredIcon icon = new ColoredIcon(owner, Style.TEXT_NORM,
                R.drawable.arrow_right, 24);
        icon.setAutoMirror(true);

        ColoredLabel label = new ColoredLabel(owner, Style.TEXT_NORM, "view_more");
        label.setFont(new Font(16));

        addViews(icon, label);
    }
}
