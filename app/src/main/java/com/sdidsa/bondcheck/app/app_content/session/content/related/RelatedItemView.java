package com.sdidsa.bondcheck.app.app_content.session.content.related;

import android.content.Context;
import android.location.Location;

import com.sdidsa.bondcheck.abs.UiCache;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.DateFormat;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;

import java.time.Duration;
import java.util.ArrayList;

public class RelatedItemView extends ColoredHBox {
    private static final ArrayList<RelatedItemView> cache = new ArrayList<>();

    public synchronized static RelatedItemView make(Context owner, Item data, Item origin) {
        cache.removeIf(item -> item.getOwner() != owner);
        RelatedItemView itemView = null;
        for(RelatedItemView iv : cache) {
            if(!iv.isAttachedToWindow()) {
                itemView = iv;
                break;
            }
        }
        if(itemView == null) {
            itemView = new RelatedItemView(owner);
            cache.add(itemView);
        }
        itemView.load(data, origin);
        return itemView;
    }

    public static void clearCache() {
        cache.clear();
    }

    static {
        UiCache.register(RelatedItemView::clearCache);
    }

    private final ColoredIcon typeIcon;
    private final ColoredLabel typeLabel;
    private final ColoredLabel relation;

    private RelatedItemView(Context owner) {
        super(owner, Style.BACK_TER);
        setPadding(15);
        setCornerRadius(10);
        setAlignment(Alignment.CENTER_LEFT);

        typeIcon = new ColoredIcon(owner, Style.TEXT_NORM, -1, 28);
        typeLabel = new ColoredLabel(owner, Style.TEXT_NORM, "")
                .setFont(new Font(18));
        relation = new ColoredLabel(owner, Style.TEXT_SEC, "same time")
                .setFont(new Font(18));

        VBox labels = new VBox(owner);
        labels.setSpacing(5);
        labels.addViews(typeLabel, relation);

        addViews(labels, SpacerUtils.spacer(owner, Orientation.HORIZONTAL), typeIcon);
    }

    private void load(Item data, Item origin) {
        typeIcon.setImageResource(data.getTypeIcon());
        typeLabel.setKey("item_type_" + data.getType());
        relation.setKey(getRelationKey(data, origin));
        setOnClickListener(e -> data.getOverlay(owner).show(data, true));
    }

    private String getRelationKey(Item data, Item origin) {
        Duration between = DateFormat.between(data.created_at(), origin.created_at()).abs();
        float[] res = new float[] {5000};
        if(data.hasLocation() && origin.hasLocation()) {
            Location.distanceBetween(
                    data.getLocation().latitude(), data.getLocation().longitude(),
                    origin.getLocation().latitude(), origin.getLocation().longitude(), res);
        }
        boolean sameTime = between.toMillis() <= 1000 * 60 * 5;
        boolean samePlace = res[0] <= 500;
        return sameTime && samePlace ? "same_time_and_place"
                : sameTime ? "same_time" :
                "same_place";
    }
}
