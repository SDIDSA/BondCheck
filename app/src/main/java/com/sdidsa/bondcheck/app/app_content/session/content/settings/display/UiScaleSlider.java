package com.sdidsa.bondcheck.app.app_content.session.content.settings.display;

import android.content.Context;
import android.view.MotionEvent;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateXAnimation;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

public class UiScaleSlider extends StackPane {
    private final int tickWidth, tickHeight;
    private final StackPane ticks;
    private final StackPane thumb;
    private int selectedIndex = 0;
    private int tickCount = 0;

    private Consumer<Integer> onChanged;

    public UiScaleSlider(Context owner) {
        super(owner);

        int baseSize = 7;

        setClipToPadding(false);
        PaddingUtils.setPaddingVertical(this, 15, owner);

        tickWidth = SizeUtils.dipToPx(baseSize / 3, owner);
        tickHeight = SizeUtils.dipToPx(baseSize * 2, owner);

        StackPane track = new ColoredStackPane(owner, Style.TEXT_MUT);
        track.setAlpha(.5f);
        track.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, SizeUtils.dipToPx(baseSize, owner)));

        ticks = new StackPane(owner);
        ticks.setLayoutParams(new LayoutParams(-1, -2));

        thumb = new ColoredStackPane(owner, Style.TEXT_SEC);
        thumb.setLayoutParams(new LayoutParams(tickHeight * 2, tickHeight * 2));
        thumb.setCornerRadius(20);

        setLayoutParams(new LayoutParams(-1, -2));

        setClipChildren(false);

        addCentered(track);
        addAligned(ticks, Alignment.CENTER_LEFT);
        addAligned(thumb, Alignment.CENTER_LEFT);
    }

    private int tickSpace;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int direction = LocaleUtils.getLocaleDirection(owner);
        if(changed) {
            ticks.removeAllViews();
            tickSpace = (right - left) / (tickCount - 1);
            for(int i = 0; i < tickCount; i++) {
                StackPane tick = getTick();
                tick.setTranslationX((i * tickSpace - tickWidth / 2f) * direction);
                ticks.addView(tick);
            }
        }
        thumb.setTranslationX((selectedIndex * tickSpace - tickHeight) * direction);
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
        setLayoutParams(getLayoutParams());
    }

    public void setSelectedIndex(int selectedIndex) {
        setSelectedIndex(selectedIndex, false);
    }
    public void setSelectedIndex(int selectedIndex, boolean noChange) {
        this.selectedIndex = selectedIndex;
        if(onChanged != null && !noChange) onChanged.accept(selectedIndex);
        setLayoutParams(getLayoutParams());
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setOnChanged(Consumer<Integer> onChanged) {
        this.onChanged = onChanged;
    }

    private Animation ticking;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                thumb.setScaleX(1.2f);
                thumb.setScaleY(1.2f);
                performClick();
            case MotionEvent.ACTION_MOVE:
                float touchX = event.getX();
                if (LocaleUtils.isRtl(owner)) {
                    touchX = getWidth() - touchX;
                }
                int atInd = (int) ((touchX + (tickSpace / 2f)) / tickSpace);
                if(atInd != selectedIndex) {
                    selectedIndex = atInd;
                    if(ticking != null && ticking.isRunning()) ticking.stop();
                    ticking = new TranslateXAnimation(200, thumb, selectedIndex * tickSpace - tickHeight)
                            .setInterpolator(Interpolator.EASE_OUT);
                    ticking.start();
                    if(onChanged != null) onChanged.accept(selectedIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
                thumb.setScaleX(1);
                thumb.setScaleY(1);
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private final ArrayList<StackPane> tickCache = new ArrayList<>();
    private StackPane getTick() {
        tickCache.removeIf(item -> item.getOwner() != owner);

        StackPane view = null;
        for(StackPane c : tickCache) {
            if(c.getParent() == null) {
                view = c;
                break;
            }
        }

        if(view == null) {
            view = new ColoredStackPane(owner, Style.TEXT_MUT);
            view.setLayoutParams(new LayoutParams(tickWidth, tickHeight));
            tickCache.add(view);
        }

        return view;
    }
}
