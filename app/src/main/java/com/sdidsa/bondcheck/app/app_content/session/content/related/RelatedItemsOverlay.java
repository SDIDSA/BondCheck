package com.sdidsa.bondcheck.app.app_content.session.content.related;

import android.content.Context;
import android.widget.ScrollView;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.SpinLoading;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlayHeader;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.RelatedItemsResponse;

import java.util.ArrayList;

import retrofit2.Call;

public class RelatedItemsOverlay extends PartialSlideOverlay {
    private static final ArrayList<RelatedItemsOverlay> cache = new ArrayList<>();
    public static RelatedItemsOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        RelatedItemsOverlay found = null;
        for(RelatedItemsOverlay inst : cache) {
            if(!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if(found == null) {
            found = new RelatedItemsOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    private final VBox items;
    private final SpinLoading loader;
    private final StackPane root;

    private RelatedItemsOverlay(Context owner) {
        super(owner, .7f);

        list.setPadding(15);
        list.setSpacing(10);

        items = new VBox(owner);
        items.setSpacing(15);

        ScrollView sv = new ScrollView(owner);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipChildren(false);

        sv.addView(items);

        loader = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);

        root = new StackPane(owner);
        root.setLayoutParams(new LayoutParams(-1,-1));
        root.setClipToOutline(true);

        root.addView(sv);

        ItemOverlayHeader header = new ItemOverlayHeader(owner);
        header.setTitle("related_events");
        header.hideInfo();
        header.hideSave();

        header.setOnClose(this::hide);

        list.addView(header);
        list.addView(root);
    }

    private void load(Item data) {
        items.removeAllViews();
        root.addCentered(loader);
        loader.startLoading();

        Call<RelatedItemsResponse> call = App.api(owner).relatedItems(data.toJSON());
        Service.enqueue(call, resp -> {
            loader.stopLoading();
            root.removeView(loader);
            if(resp.isSuccessful() && resp.body() != null) {
                resp.body().getCombined().forEach(item -> {
                    RelatedItemView iv = RelatedItemView.make(owner, item, data);
                    iv.setAlpha(0f);
                    items.addViews(iv);
                });
                Animation.sequenceFadeInUp(owner, ContextUtils.getViewChildren(items))
                        .setInterpolator(Interpolator.OVERSHOOT)
                        .start();
            }
        });

    }

    public void show(Item data) {
        load(data);
        super.show();
    }
}
