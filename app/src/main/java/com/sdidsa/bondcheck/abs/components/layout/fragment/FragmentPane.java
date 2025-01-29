package com.sdidsa.bondcheck.abs.components.layout.fragment;

import android.content.Context;
import android.widget.FrameLayout;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateXAnimation;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;

import java.util.concurrent.Semaphore;

public class FragmentPane extends FrameLayout {
    protected final Context owner;
    Animation running = null;
    private Fragment loaded;
    private final Class<? extends Fragment> type;

    public FragmentPane(Context owner) {
        this(owner, Fragment.class);
    }

    public FragmentPane(Context owner, Class<? extends Fragment> type) {
        super(owner);
        this.owner = owner;
        this.type = type;
        setClipChildren(false);
        if(type.equals(Fragment.class)) {
            ErrorHandler.handle(new IllegalArgumentException("you shouldn't use raw Fragment type"),
                    "create fragment pane of type Fragment");
        }
        Fragment.clearCache(type);
    }

    public void nextInto(Class<? extends Fragment> pageType, Runnable post) {
        navigateInto(pageType, 1, post);
    }

    public void nextInto(Class<? extends Fragment> pageType) {
        navigateInto(pageType, 1, null);
    }

    public void previousInto(Class<? extends Fragment> pageType, Runnable post) {
        navigateInto(pageType, -1, post);
    }

    public void previousInto(Class<? extends Fragment> pageType) {
        navigateInto(pageType, -1, null);
    }

    private final Semaphore mutex = new Semaphore(1);
    private void navigateInto(Class<? extends Fragment> fragmentType, int direction,
                              Runnable post) {
        if (!type.isAssignableFrom(fragmentType)) {
            ErrorHandler.handle(new IllegalArgumentException("incompatible types"),
                    "navigate to fragment of type " + fragmentType.getName() +
                    "in a fragment pane of type " + type.getName());
        }

        Platform.runLater(() -> ContextUtils.hideKeyboard(owner));

        Platform.runBack(() -> {
            mutex.acquireUninterruptibly();
            Platform.waitWhile(this::isRunning);

            Fragment old = loaded;
            if (old != null && fragmentType.isInstance(old)) {
                if(post != null) post.run();
                return;
            }

            float fromX = old == null ? 0 : (ContextUtils.getScreenWidth(owner) * direction) / (direction == 1 ? 1f : 2f);
            float toX = -(ContextUtils.getScreenWidth(owner) * direction) / (direction == 1 ? 2f : 1f);

            Fragment nw = Fragment.getInstance(owner, fragmentType);
            if (nw == null) return;

            loaded = nw;
            loaded.setup(direction == 1);

            ParallelAnimation showNew = new ParallelAnimation(300)
                    .setInterpolator(Interpolator.OVERSHOOT);
            showNew.addAnimation(new TranslateXAnimation(loaded, fromX / 2, 0));
            showNew.addAnimation(Animation.fadeIn(loaded));
            if(old != null) {
                showNew.addAnimation(new TranslateXAnimation(old, 0, toX / 2));
                showNew.addAnimation(Animation.fadeOut(old));
            }
            showNew.setOnFinished(() -> {
                if(post != null) post.run();
                if(old != null) {
                    old.destroy(direction == 1);
                    removeView(old);
                }
            });

            running = showNew;

            Platform.runLater(() -> {
                loaded.setPane(this);
                showNew.start();
                addView(loaded);
                mutex.release();
            });
        });
    }

    public boolean isRunning() {
        return running != null && running.isRunning();
    }

    public Fragment getLoaded() {
        return loaded;
    }
}
