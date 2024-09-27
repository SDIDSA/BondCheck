package com.sdidsa.bondcheck.app.app_content.session.content.records;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.sdidsa.bondcheck.abs.components.controls.shape.ColoredRectangle;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;

import java.util.function.Consumer;

public class PlayerProgress extends StackPane {
    private final Property<Float> progress;
    private final Property<Float> size;

    private final ColoredRectangle thumb;

    private Runnable onStartSeeking;
    private Consumer<Float> onSeek;
    private Consumer<Float> onSeeking;

    public PlayerProgress(Context owner) {
        super(owner);

        progress = new Property<>(0f);
        size = new Property<>(0f);

        setClipToOutline(false);
        setClipToPadding(false);

        ColoredStackPane track = new ColoredStackPane(owner, Style.BACK_TER);
        ColoredStackPane progressTrack = new ColoredStackPane(owner, Style.TEXT_MUT);
        thumb = new ColoredRectangle(owner, Style.TEXT_NORM);
        thumb.setElevation(ContextUtils.dipToPx(7, owner));

        size.addListener((ov, nv) -> {
            ViewGroup.LayoutParams params = track.getLayoutParams();
            if(params == null) params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.width = LayoutParams.MATCH_PARENT;
            params.height = ContextUtils.dipToPx(nv, owner);
            track.setLayoutParams(params);
            track.setCornerRadius(nv);
            thumb.setSize(nv * 3, nv * 3);
            thumb.setRadius(nv * 3);
            Platform.runLater(() -> progress.set(progress.get()));
        });

        progress.addListener((ov, nv) -> {ViewGroup.LayoutParams params = progressTrack.getLayoutParams();
            if(params == null) params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.width = (int) (getWidth() * nv);
            params.height = ContextUtils.dipToPx(size.get(), owner);
            progressTrack.setLayoutParams(params);
            progressTrack.setCornerRadius(size.get());
            thumb.setTranslationX(((getWidth() - (params.height * 3)) * nv)
                    * ContextUtils.getLocaleDirection(owner));
        });

        size.set(10f);
        progress.set(0f);

        addCentered(track);
        addAligned(progressTrack, Alignment.CENTER_LEFT);
        addAligned(thumb, Alignment.CENTER_LEFT);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setOnStartSeeking(Runnable onStartSeeking) {
        this.onStartSeeking = onStartSeeking;
    }

    public void setOnSeek(Consumer<Float> onSeek) {
        this.onSeek = onSeek;
    }

    public void setOnSeeking(Consumer<Float> onSeeking) {
        this.onSeeking = onSeeking;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getPointerCount() > 1) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                if(onStartSeeking != null) {
                    onStartSeeking.run();
                }
                posFromEvent(event);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                posFromEvent(event);
                if(onSeeking != null) {
                    onSeeking.accept(progress.get());
                }
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if(onSeek != null) {
                    onSeek.accept(progress.get());
                }
                performClick();
                return true;
            }
        }

        return false;
    }

    private void posFromEvent(MotionEvent event) {
        float x = event.getX() - thumb.getWidth() / 2f;
        if(ContextUtils.isRtl(owner)) {
            x = getWidth() - x;
            x -= thumb.getWidth();
        }
        float p = x / (getWidth() - thumb.getWidth());
        p = p > 1 ? 1 : p < 0 ? 0 : p;
        progress.set(p);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public Property<Float> sizeProperty() {
        return size;
    }

    public float getSize() {
        return size.get();
    }

    public void setSize(float size) {
        this.size.set(size);
    }

    public Property<Float> progressProperty() {
        return progress;
    }

    public float getProgress() {
        return progress.get();
    }

    public void setProgress(float progress) {
        this.progress.set(progress);
    }
}
