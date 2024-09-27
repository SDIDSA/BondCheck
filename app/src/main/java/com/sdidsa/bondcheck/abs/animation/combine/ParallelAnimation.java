package com.sdidsa.bondcheck.abs.animation.combine;

import com.sdidsa.bondcheck.abs.animation.base.Animation;

import java.util.ArrayList;

public class ParallelAnimation extends Animation implements CombineAnimation{
    private final ArrayList<Animation> animations;

    private Runnable onFinish;

    public ParallelAnimation(long duration) {
        super(duration);

        animations = new ArrayList<>();

        super.setOnFinished(() -> {
           for(Animation animation : animations) {
               Runnable onFinished = animation.getOnFinished();
               if(onFinished != null)
                   onFinished.run();
           }
           if(onFinish != null) onFinish.run();
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Animation> T setOnFinished(Runnable onFinished) {
        onFinish = onFinished;
        return (T) this;
    }

    public ParallelAnimation addAnimations(Animation...animations) {
        for(Animation a : animations) {
            addAnimation(a);
        }
        return this;
    }

    public ParallelAnimation addAnimation(Animation animations) {
        this.animations.add(animations);
        return this;
    }

    public ArrayList<Animation> getAnimations() {
        return animations;
    }

    @Override
    public void init() {
        for (Animation a : animations) {
            a.init();
        }
        super.init();
    }

    @Override
    public void update(float v) {
        for (Animation a : animations) {
            a.update(v);
        }
    }
}
