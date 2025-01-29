package com.sdidsa.bondcheck.app.app_content.session.content.screenshots;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.UiCache;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.scale.ScaleXYAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.image.ZoomageView;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.Overlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemDetailsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlayHeader;
import com.sdidsa.bondcheck.models.responses.PostResponse;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import java.util.ArrayList;
import java.util.Date;

public class ScreenshotOverlay extends Overlay implements ItemOverlay {
    private static final ArrayList<ScreenshotOverlay> cache = new ArrayList<>();

    public static ScreenshotOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        ScreenshotOverlay found = null;
        for (ScreenshotOverlay inst : cache) {
            if (!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if (found == null) {
            found = new ScreenshotOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    public static void clearCache() {
        cache.clear();
    }

    static {
        UiCache.register(ScreenshotOverlay::clearCache);
    }

    private final ItemOverlayHeader controls;
    private ScreenshotResponse screen;
    private final ZoomageView view;

    private final HBox others;

    private ScreenshotOverlay(Context owner) {
        super(owner);
        VBox cont = new VBox(owner);
        cont.setSpacing(15);
        cont.setLayoutParams(new LayoutParams(-1, -1));
        cont.setPadding(20);
        cont.setClipChildren(true);

        cont.setScaleY(.75f);
        cont.setScaleX(.75f);
        cont.setAlpha(0);

        view = new ZoomageView(owner);
        view.setRestrictBounds(true);
        view.setOnScale((ov, nv) -> {
            if (ov > 1 && nv == 1) {
                showControls();
            } else if (ov == 1 && nv > 1) {
                hideControls();
            }
        });

        view.setOnSingleTap(() -> {
            if (controlsShown) {
                hideControls();
            } else {
                showControls();
            }
        });

        controls = new ItemOverlayHeader(owner);
        PaddingUtils.setPadding(controls, 15, 15, 10, 15, owner);
        controls.setFill(s -> App.adjustAlpha(s.getBackgroundTertiary(), .85f));
        controls.setOnClose(this::hide);
        controls.setOnInfo(() -> ItemDetailsOverlay.getInstance(owner).show(screen));

        StackPane preCont = new ColoredStackPane(owner, Style.BACK_TER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.weight = 1;
        preCont.setLayoutParams(params);
        preCont.addView(view);
        preCont.addView(controls);

        preCont.setCornerRadius(15);
        preCont.setOutlineProvider(new OutlineProvider());
        preCont.setClipToOutline(true);

        others = new HBox(owner);
        HorizontalScrollView sv = new HorizontalScrollView(owner);
        sv.setHorizontalScrollBarEnabled(false);
        sv.addView(others);

        cont.addView(preCont);
        cont.addView(sv);

        addToShow(new ScaleXYAnimation(cont, 1));
        addToShow(new AlphaAnimation(cont, 1));

        addToHide(new ScaleXYAnimation(cont, .75f));
        addToHide(new AlphaAnimation(cont, 0));

        addView(cont);
    }

    private void load(ScreenshotResponse screen) {
        this.screen = screen;
        controls.setUser(screen.provider());
        view.setImageResource(R.drawable.empty);
        ImageProxy.getImage(owner, screen.asset_id(), bmp -> {
            view.setImageBitmap(bmp);
            Platform.waitWhile(() -> !view.isReady(), view::reset);
            showControls();
        });
        controls.setOnSave(() -> saveImage(screen.asset_id(), "screenshot", screen.created_at()));
        controls.setTitleText(screen.app());

        others.removeAllViews();
        otherImages.clear();
    }

    private String loadedUrl;
    private final ArrayList<NetImage> otherImages = new ArrayList<>();
    private void load(PostResponse post, String url) {
        this.screen = null;
        loadedUrl = url;
        view.setImageResource(R.drawable.empty);
        ImageProxy.getImage(owner, url, bmp -> {
            view.setImageBitmap(bmp);
            Platform.waitWhile(() -> !view.isReady(), view::reset);
            showControls();
        });

        others.removeAllViews();
        otherImages.clear();
        String[] urls = post.getMedia();
        if(urls != null && urls.length > 1) {
            for(int i = 0; i < urls.length; i++) {
                String u = urls[i];
                NetImage o = new NetImage(owner, Style.BACK_TER);
                o.setImageThumbUrl(u, 72);
                o.setCornerRadius(15);
                o.setOnClick(() -> {
                    if(u.equalsIgnoreCase(loadedUrl)) return;
                    loadedUrl = u;
                    view.setImageResource(R.drawable.empty);
                    ImageProxy.getImage(owner, u, bmp -> {
                        view.setImageBitmap(bmp);
                        Platform.waitWhile(() -> !view.isReady(), view::reset);
                        showControls();
                    });
                    for(NetImage img : otherImages) {
                        img.setViewAlpha(img.getUrl().equalsIgnoreCase(u) ? 1 : 0.4f);
                    }
                    controls.setOnSave(() -> saveImage(u,
                            "post_" + post.indexOf(u),
                            post.created_at()));
                });
                if(i > 0) {
                    MarginUtils.setMarginLeft(o, owner, 15);
                }
                others.addView(o);
                o.setViewAlpha(u.equalsIgnoreCase(url) ? 1 : 0.4f);
                otherImages.add(o);
            }
        }

        controls.setOnSave(() -> saveImage(url, "post_" + post.indexOf(url), post.created_at()));
        controls.setTitleText(post.content());
    }

    public void show(Item item) {
        show(item, false);
    }

    public void show(PostResponse post, String url) {
        controls.showInfo(false);
        load(post, url);
        super.show();
    }

    public void show(Item item, boolean related) {
        if (item instanceof ScreenshotResponse data) {
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
        if (controlsShown) return;
        if (showControls == null) {
            showControls = Animation.fadeInDown(owner, controls)
                    .setInterpolator(Interpolator.EASE_OUT);
        }
        controls.setAlpha(0);
        if (hideControls != null) hideControls.stop();
        controlsShown = true;
        showControls.start();
    }

    private Animation hideControls;

    public void hideControls() {
        if (!controlsShown) return;
        if (hideControls == null) {
            hideControls = Animation.fadeOutUp(owner, controls)
                    .setInterpolator(Interpolator.EASE_OUT);
        }
        if (showControls != null) showControls.stop();
        controlsShown = false;
        hideControls.start();
    }

    private void saveImage(String url, String context, Date date) {
        ImageProxy.getImage(owner, url, bmp ->
                Platform.runBack(() -> {
                    int res = ImageProxy.saveImageToGallery(owner, bmp,
                            context + "_" + date.getTime());
                    switch (res) {
                        case ImageProxy.FILE_SAVED ->
                                ContextUtils.toast(owner, "Image saved");
                        case ImageProxy.FILE_EXISTS -> ContextUtils.toast(owner,
                                "This image has already been saved");
                        case ImageProxy.FILE_ERROR -> ContextUtils.toast(owner,
                                "Something went wrong, retry later");
                    }
                }));
    }

    @Override
    public void applySystemInsets(Insets insets) {
        setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    private class OutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0, 0,
                    view.getWidth(), view.getHeight()), SizeUtils.dipToPx(15, owner));
        }

    }
}
