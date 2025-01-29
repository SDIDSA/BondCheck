package com.sdidsa.bondcheck.app.app_content.session.content.main.create.media;

import android.content.Context;
import android.widget.HorizontalScrollView;

import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.data.media.Media;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;

import java.util.ArrayList;

public class PostMedia extends HorizontalScrollView {
    private final Context owner;
    private final HBox root;

    private final ArrayList<Media> media;

    public PostMedia(Context owner) {
        super(owner);
        setHorizontalScrollBarEnabled(false);

        this.owner = owner;
        this.media = new ArrayList<>();

        root = new HBox(owner);
        PaddingUtils.setPadding(root, 0,10,10,0,owner);
        addView(root);
    }

    public void add() {
        ContextUtils.pickImage(owner, res -> {
            if(res != null) {
                PostMediaItem img = new PostMediaItem(owner, res);
                MarginUtils.setMarginLeft(img, owner, 10);
                img.setOnClickListener(v -> img.hide(() -> {
                    media.remove(res);
                    root.removeView(img);
                }));
                media.add(res);
                root.addView(img);
            }
        });
    }

    public ArrayList<Media> getMedia() {
        return media;
    }

    public void reset() {
        media.clear();
        root.removeAllViews();
    }
}
