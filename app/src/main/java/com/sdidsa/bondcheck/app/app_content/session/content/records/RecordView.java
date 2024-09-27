package com.sdidsa.bondcheck.app.app_content.session.content.records;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.audio.AudioProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.app.app_content.session.content.main.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.models.responses.RecordResponse;

import java.util.ArrayList;

public class RecordView extends ItemView {
    private static final ArrayList<RecordView> cache = new ArrayList<>();

    public synchronized static RecordView make(Context owner, RecordResponse record) {
        cache.removeIf(item -> item.getOwner() != owner);

        RecordView view = null;
        for(RecordView c : cache) {
            if(c.getParent() == null) {
                view = c;
                break;
            }
        }

        if(view == null) {
            view = new RecordView(owner);
            cache.add(view);
        }

        view.loadRecord(record);

        return view;
    }

    private RecordView(Context owner) {
        super(owner);

        setPadding(15);
        setCornerRadius(15);

        ColoredIcon img = new ColoredIcon(owner, Style.TEXT_SEC, Style.BACK_SEC,
                R.drawable.play);
        img.setAutoMirror(true);
        img.setCornerRadius(10);
        img.setPadding(HomeSection.ITEM_SIZE / 3.2f);
        img.setSize(HomeSection.ITEM_SIZE);
        addViews(img);
    }

    private void loadRecord(RecordResponse record) {
        super.loadItem(record);
        AudioProxy.getAudio(owner, record.asset_id(),
                file -> {
                    int seconds = (int) ((file.duration() + 500) / 1000);
                    second.setKey("duration_seconds", Integer.toString(seconds));
                });

        setOnClickListener((e) -> RecordOverlay.getInstance(owner).show(record));
    }
}
