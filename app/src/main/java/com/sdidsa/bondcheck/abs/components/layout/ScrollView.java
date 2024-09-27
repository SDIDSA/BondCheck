package com.sdidsa.bondcheck.abs.components.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.base.ValueAnimation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateYAnimation;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

import java.util.function.Consumer;

public class ScrollView extends android.widget.ScrollView {
    private final float DOWN_PX;
    private final Context owner;
    private Consumer<Float> onMaybeRefresh;
    private Runnable onRefresh;

    private final StackPane root;
    private final Label refreshLabel;

    public ScrollView(Context context) {
        super(context);
        this.owner = context;

        root = new StackPane(owner);
        refreshLabel = new ColoredLabel(owner, Style.TEXT_NORM, "Release to refresh")
                .setFont(new Font(20));

        super.addView(root, 0);

        ContextUtils.setMarginTop(refreshLabel, owner, 30);

        setFillViewport(true);

        DOWN_PX = ContextUtils.dipToPx(80, owner);

        ContextUtils.spacer(this, Orientation.VERTICAL);
        setVerticalScrollBarEnabled(false);
        setClipChildren(false);


    }

    private int initY=-1, initScroll=-1;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleTouch(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        handleTouch(ev);
        return super.onTouchEvent(ev);
    }

    private void handleTouch(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_MOVE) {
            if(initY == -1) {
                initY = (int) ev.getRawY();
                initScroll = getScrollY();
            } else {
                int y = (int) ev.getRawY();
                int scroll = getScrollY();
                int diffY = y - initY;
                int diffScroll = initScroll - scroll;
                int add = diffY - diffScroll;
                if(add > 20) {
                    float dist = ContextUtils.pxToDip(add - 20, owner) / 200;
                    dist = dist < 0 ? 0 : dist > 1 ? 1 : dist;
                    stockMaybeRefresh(dist);
                }
            }
        }
        if(ev.getAction() == MotionEvent.ACTION_UP) {
            if(initY != -1) {
                int y = (int) ev.getRawY();
                int scroll = getScrollY();
                int diffY = y - initY;
                int diffScroll = initScroll - scroll;
                int add = diffY - diffScroll;
                if(add > 20) {
                    float dist = ContextUtils.pxToDip(add - 20, owner) / 200;
                    dist = dist < 0 ? 0 : dist > 1 ? 1 : dist;
                    if(dist >= 1) {
                        stockRefresh();
                    } else if(dist < 1) {
                        new ValueAnimation(300, dist, 0) {
                            @Override
                            public void updateValue(float v) {
                                stockMaybeRefresh(v);
                            }
                        }.setInterpolator(Interpolator.EASE_OUT)
                                .start();
                    }
                }
            }

            initY = -1;
            initScroll = -1;
        }
    }

    private void stockMaybeRefresh(float dist) {
        if(onMaybeRefresh != null) {
            removeView(refreshLabel);
            addAligned(refreshLabel, Alignment.TOP_CENTER);
            onMaybeRefresh.accept(dist);
            refreshLabel.setAlpha(dist < 1 ? dist / 2 : 1);
            refreshLabel.setTranslationY(DOWN_PX * dist - DOWN_PX);
            getContent().setAlpha(1 - (dist * .5f));
            getContent().setTranslationY(DOWN_PX * dist);
            refreshLabel.setText(dist < 1 ? "drag to refresh" : "release to refresh");
            if(dist <= 0) {
                removeView(refreshLabel);
            }
        }
    }

    private void stockRefresh() {
        if(onRefresh != null) {
            onRefresh.run();
            new ParallelAnimation(300)
                    .addAnimation(new AlphaAnimation(getContent(), 1))
                    .addAnimation(new TranslateYAnimation(getContent(), 0))
                    .addAnimation(Animation.fadeOutUp(owner, refreshLabel))
                    .setInterpolator(Interpolator.EASE_OUT)
                    .setOnFinished(() -> removeView(refreshLabel))
                    .start();
        }
    }

    private View getContent() {
        for(int i = 0; i < root.getChildCount(); i++) {
            if(root.getChildAt(i) != refreshLabel) {
                return root.getChildAt(i);
            }
        }
        return this;
    }

    public void setOnMaybeRefresh(Consumer<Float> onMaybeRefresh) {
        this.onMaybeRefresh = onMaybeRefresh;
        setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void addCentered(View child) {
        root.addCentered(child);
    }

    public void addAligned(View child, Alignment alignment) {
        root.addAligned(child, alignment);
    }

    @Override
    public void addView(View child) {
        root.addView(child);
    }

    public void addViews(View... views) {
        root.addViews(views);
    }

    @Override
    public void removeView(View view) {
        root.removeView(view);
    }

    @Override
    public void removeAllViews() {
        root.removeAllViews();
    }

    @Override
    public void addView(View child, int index) {
        root.addView(child, index);
    }
}
