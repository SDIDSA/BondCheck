package com.sdidsa.bondcheck.app.app_content.session.content.screenshots;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.scale.ScaleXYAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.ZoomageView;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.overlay.Overlay;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemDetailsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlayHeader;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import java.util.ArrayList;

public class ScreenshotOverlay extends Overlay implements ItemOverlay {
    private static final ArrayList<ScreenshotOverlay> cache = new ArrayList<>();
    public static ScreenshotOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        ScreenshotOverlay found = null;
        for(ScreenshotOverlay inst : cache) {
            if(!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if(found == null) {
            found = new ScreenshotOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    private final ItemOverlayHeader controls;
    private ScreenshotResponse screen;
    private Bitmap bmp;
    private final ZoomageView view;

    private ScreenshotOverlay(Context owner) {
        super(owner);
        StackPane cont = new StackPane(owner);
        cont.setPadding(20);
        cont.setClipChildren(true);

        cont.setScaleY(.75f);
        cont.setScaleX(.75f);
        cont.setAlpha(0);

        view = new ZoomageView(owner);
        view.setRestrictBounds(true);
        view.setOnScale((ov, nv) -> {
            if(ov > 1 && nv == 1) {
                showControls();
            } else if(ov == 1 && nv > 1) {
                hideControls();
            }
        });

        view.setOnSingleTap(() -> {
            if(controlsShown) {
                hideControls();
            } else {
                showControls();
            }
        });

        controls = new ItemOverlayHeader(owner);
        ContextUtils.setPadding(controls, 15, 15, 10, 15, owner);
        controls.setFill(s -> App.adjustAlpha(s.getBackgroundPrimary(), .8f));
        controls.setOnSave(this::saveImage);
        controls.setOnClose(this::hide);
        controls.setOnInfo(() -> ItemDetailsOverlay.getInstance(owner).show(screen));

        StackPane preCont = new ColoredStackPane(owner, s ->
                App.adjustAlpha(s.getBackgroundPrimary(), .3f));
        preCont.addView(view);
        preCont.addView(controls);

        preCont.setCornerRadius(15);
        preCont.setOutlineProvider(new OutlineProvider());
        preCont.setClipToOutline(true);

        cont.addView(preCont);

        addToShow(new ScaleXYAnimation(cont, 1));
        addToShow(new AlphaAnimation(cont, 1));

        addToHide(new ScaleXYAnimation(cont, .75f));
        addToHide(new AlphaAnimation(cont, 0));

        addView(cont);
    }

    private void load(ScreenshotResponse screen) {
        this.screen = screen;
        view.setImageResource(R.drawable.empty);
        ImageProxy.getImage(owner, screen.asset_id(), bmp -> {
            view.setImageBitmap(bmp);
            Platform.waitWhile(() -> !view.isReady(), view::reset);
            showControls();
            this.bmp = bmp;
        });
        controls.setTitleText(screen.app());
    }

    public void show(Item item) {
        show(item, false);
    }

    public void show(Item item, boolean related) {
        if(item instanceof ScreenshotResponse data) {
            controls.showInfo(!related);
            load(data);
            super.show();
        } else {
            ErrorHandler.handle(
                    new IllegalArgumentException("wrong item type, should be ScreenshotResponse"),
                    "loading ScreenshotOverlay");
        }
    }

    @Override
    public void show() {
        ErrorHandler.handle(new IllegalAccessError(
                "can't show without loading a screen object, " +
                        "use show(ScreenshotResponse) instead"),
                "showing ScreenshotOverlay");
    }

    private boolean controlsShown = false;
    private Animation showControls;
    public void showControls() {
        if(controlsShown) return;
        if(showControls == null) {
            showControls = Animation.fadeInDown(owner, controls)
                    .setInterpolator(Interpolator.EASE_OUT);
        }
        controls.setAlpha(0);
        if(hideControls != null) hideControls.stop();
        controlsShown = true;
        showControls.start();
    }

    private Animation hideControls;
    public void hideControls() {
        if(!controlsShown) return;
        if(hideControls == null) {
            hideControls = Animation.fadeOutUp(owner, controls)
                    .setInterpolator(Interpolator.EASE_OUT);
        }
        if(showControls != null) showControls.stop();
        controlsShown = false;
        hideControls.start();
    }

    private void saveImage() {
        if(bmp == null) {
            ContextUtils.toast(owner, "loading...");
        }
        int res = ImageProxy.saveImageToGallery(owner, bmp,
                ContextUtils.getAppName(owner).toLowerCase() +
                        "_" + screen.id() + ".jpg");

        switch (res) {
            case ImageProxy.FILE_SAVED ->
                    ContextUtils.toast(owner, "Image saved");
            case ImageProxy.FILE_EXISTS ->
                    ContextUtils.toast(owner,
                            "This image has already been saved");
            case ImageProxy.FILE_ERROR ->
                    ContextUtils.toast(owner,
                            "Something went wrong, retry later");
        }
    }

    @Override
    public void applySystemInsets(Insets insets) {
        setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    private class OutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0,0,
                    view.getWidth(), view.getHeight()), ContextUtils.dipToPx(15, owner));
        }

    }
}
