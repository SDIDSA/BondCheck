package com.sdidsa.bondcheck.app.app_content.session.content.main.create.location;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.scroll.RecyclerItemView;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.CreateMain;

public class LocationDisplay extends RecyclerItemView<LocationDetail> {
    private final ColoredIcon img;
    private final ColoredLabel where;
    private final ColoredLabel description;
    private final VBox lines;

    private LocationDetail item;

    public LocationDisplay(Context owner) {
        super(owner);
        HBox root = new HBox(owner);
        root.setAlignment(Alignment.CENTER_LEFT);

        img = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.empty, 40);
        img.setPadding(7);

        where = new ColoredLabel(owner, Style.TEXT_SEC, "").setFont(new Font(18));
        where.setSingleLine();
        description = new ColoredLabel(owner, Style.TEXT_NORM, "")
                .setFont(new Font(18));
        description.setSingleLine();

        lines = new VBox(owner);
        lines.setAlignment(Alignment.CENTER_LEFT);
        lines.setSpacing(5);
        lines.addViews(description, where);
        MarginUtils.setMarginLeft(lines, owner, 15);

        root.setOnClickListener(e -> Fragment.getInstance(owner, CreateMain.class).onLocation(item));

        root.addView(img);
        root.addView(lines);
        addView(root);
    }

    @Override
    public void load(LocationDetail item) {
        this.item = item;
        lines.removeView(where);
        if(item.region().isBlank() || item.country().isBlank()) {
            where.setKey("");
        } else {
            where.setKey(item.region() + ", " + item.country());
            lines.addView(where);
        }
        description.setKey(item.label());
        img.setImageResource(item.type().getIcon());
    }
}
