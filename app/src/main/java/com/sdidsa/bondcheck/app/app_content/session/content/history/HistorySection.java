package com.sdidsa.bondcheck.app.app_content.session.content.history;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.shared.HomeSection;

public class HistorySection extends HomeSection {
    protected boolean ready = false;
    protected final Property<String> other_id;
    protected final HBox root;
    protected final ViewMore more;
    private Runnable onMore;

    public HistorySection(Context owner) {
        this(owner, "Section Title", -1);
    }

    public HistorySection(Context owner, String title, @DrawableRes int icon) {
        super(owner, title, icon);

        other_id = new Property<>("");

        root = new HBox(owner);
        root.setPadding(5);

        root.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                ContextUtils.dipToPx(HomeSection.ITEM_SIZE + 10, owner)));

        more = new ViewMore(owner);

        more.setOnClickListener(e -> {
            if(onMore != null) {
                onMore.run();
            }
        });

        addToContent(root);
    }

    public void addItem(HistoryThumbnail thumb, int index) {
        root.addView(thumb, index);
        ContextUtils.setMarginRight(thumb, owner, 15);
    }

    public void clearItems() {
        root.removeAllViews();
        root.addView(more);
    }

    public boolean isReady() {
        return ready;
    }

    public void setOnMore(Runnable onMore) {
        this.onMore = onMore;
    }

    public Property<String> otherUser() {
        return other_id;
    }
}
