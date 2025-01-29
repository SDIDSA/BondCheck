package com.sdidsa.bondcheck.app.app_content.session.content.item_display;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateYAnimation;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Refresh;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.controls.text.transformationMethods.Capitalize;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;
import com.sdidsa.bondcheck.app.app_content.session.content.history.History;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketEventListener;

public abstract class Items extends HomePage {
    protected final VBox items;
    protected final Refresh refresh;
    protected final StackPane preItems;
    protected final ColoredButton request;

    protected final ColoredSpinLoading loader;

    protected final SocketEventListener socket;

    public Items(Context owner, String title, String requestText, @DrawableRes int requestIcon) {
        super(owner, title);

        socket = Action.socketEventReceiver(owner);

        content.setSpacing(20);
        content.setPadding(0);

        items = new VBox(owner);
        items.setSpacing(15);
        PaddingUtils.setPadding(items, 15, 0, 15, 15, owner);

        refresh = new Refresh(owner);
        refresh.setOnClick(this::fetch);

        ColoredIcon back = new ColoredIcon(owner, Style.TEXT_NORM, R.drawable.arrow_left, 48);
        back.setPadding(12);
        back.setAutoMirror(true);
        back.setContentDescription("Go Back to history page");
        MarginUtils.setMarginRight(back, owner, 15);
        back.setOnClick(() -> {
            Home home = Page.getInstance(owner, Home.class);
            home.previousInto(History.class, null);
        });

        top.addView(back, 0);
        top.addView(refresh);

        loader = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);

        preItems = new StackPane(owner);
        preItems.addView(items);
        preItems.setClipChildren(false);
        MarginUtils.setMarginTop(loader, owner, 128);

        request = new ColoredButton(owner, Style.BACK_SEC, Style.TEXT_NORM,
                requestText);
        request.setTransformationMethod(new Capitalize());
        request.setFont(new Font(20, FontWeight.MEDIUM));
        request.setPadding(20);
        request.extendLabel();
        request.addPostLabel(new ColoredIcon(owner, Style.TEXT_NORM, requestIcon, 26));
        MarginUtils.setMarginUnified(request, owner, 15);

        request.setOnClick(this::requestItem);

        content.addViews(preItems);
        root.addView(request);

        scrollable.setOnRefreshGesture(this::onMaybeRefresh);
        scrollable.setOnRefresh(this::onRefresh);

        fetch();
    }

    public void onMaybeRefresh(float dist) {
        refresh.applyRefresh(dist);
    }

    public void onRefresh() {
        refresh.fire();
    }

    @Override
    public void clear() {
        super.clear();
        socket.unregister(owner);
    }

    protected abstract void requestItem();

    protected boolean fetching = false;
    protected abstract void fetch();

    @Override
    public void setup(boolean direction) {
        super.setup(direction);

        if(!fetching) {
            Platform.runBack(() -> showAll(null));
        }
    }

    private Animation showLoader;
    protected Animation showLoader() {
        if(showLoader == null) {
            showLoader = new ParallelAnimation(300)
                    .addAnimation(Animation.fadeInUp(owner, loader))
                    .setInterpolator(Interpolator.EASE_OUT);
        }
        loader.startLoading();
        loader.setAlpha(0);
        loader.setTranslationY(0);
        preItems.addAligned(loader, Alignment.TOP_CENTER);
        if(hideLoader != null && hideLoader.isRunning()) {
            hideLoader.stop();
        }
        return showLoader;
    }

    private Animation hideLoader;
    protected Animation hideLoader() {
        if(hideLoader == null) {
            hideLoader = new ParallelAnimation(300)
                    .addAnimation(Animation.fadeOutUp(owner, loader))
                    .setInterpolator(Interpolator.EASE_OUT)
                    .setOnFinished(() -> {
                        loader.stopLoading();
                        preItems.removeView(loader);
                    });
        }
        if(showLoader != null && showLoader.isRunning()) {
            showLoader.stop();
        }
        return hideLoader;
    }

    protected void showAll(Runnable onFinished) {
        Animation an = Animation.sequenceFadeInUp(owner, ContextUtils.getViewChildren(items));
        an.setOnFinished(onFinished);
        an.start();
    }

    protected void hideAll(Runnable onFinished) {
       Animation.fadeOut(ContextUtils.getViewChildren(items))
                .setOnFinished(onFinished)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    protected Animation dragDown() {
        ParallelAnimation res = new ParallelAnimation(300)
                .setInterpolator(Interpolator.OVERSHOOT);
        int spacing = SizeUtils.dipToPx(20, owner);
        for (int i = 0; i < items.getChildCount(); i++) {
            if (items.getChildAt(i) instanceof ItemView item) {
                if(i < 2) {
                    res.addAnimation(
                            new TranslateYAnimation(item, item.getHeight() + spacing));
                } else {
                    res.addAnimation(item.hide().setOnFinished(() ->
                            items.removeView(item)));
                }
            }
        }
        return res;
    }

    protected void reset() {
        for (int i = 0; i < items.getChildCount(); i++) {
            if (items.getChildAt(i) instanceof ItemView item) {
                item.setTranslationY(0);
            }
        }
    }
}
