package com.sdidsa.bondcheck.abs.components.layout.overlay.media.medialist;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.media.Media;

import java.util.List;
import java.util.function.Consumer;

public class MediaList extends RecyclerView {
    public static final int SPAN_COUNT = 3;
    private final MediaAdapter adapter;

    private Consumer<List<Media>> onData;

    private final Property<Media> selected;

    public MediaList(Context owner) {
        this(owner, null);
    }

    public MediaList(Context owner, Property<Media> selected) {
        super(owner);
        this.selected = selected;

        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(true);

        GridLayoutManager lm = new GridLayoutManager(owner, SPAN_COUNT);
        lm.setSmoothScrollbarEnabled(true);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(lm);

        int size = ContextUtils.getScreenWidth(owner) - ContextUtils.dipToPx(50, owner);
        adapter = new MediaAdapter(owner, size, selected);
        setAdapter(adapter);

        GradientDrawable clip = new GradientDrawable();
        clip.setCornerRadius(ContextUtils.dipToPx(12, owner));
        setBackground(clip);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);

        setClipToPadding(false);
    }

    public void setOnData(Consumer<List<Media>> onData) {
        this.onData = onData;
    }

    public void setData(List<Media> data) {
        adapter.setData(data);
        scrollToPosition(0);
        if(!data.contains(selected.get())) {
            selected.set(null);
        }

        if(onData != null) {
            onData.accept(data);
        }
    }
}