package com.sdidsa.bondcheck.app.app_content.session.navbar;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.animation.base.ColorAnimation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.CornerRadiusAnimation;
import com.sdidsa.bondcheck.abs.animation.view.WidthAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateXAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateYAnimation;
import com.sdidsa.bondcheck.abs.animation.view.scale.ScaleXYAnimation;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.abs.CornerUtils;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.view.AlignUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;

public class NavBarItem extends StackPane implements Localized {
    private Runnable onClick;

    private final NavBarIcon icon;

    private Runnable onSelected;
    private Runnable postOnSelected;

    private final NavBar parent;

    public NavBarItem(Context owner){
        this(owner, null,  -1, -1, null);
    }

    public NavBarItem(Context owner, NavBar parent,
                      @DrawableRes int fill, @DrawableRes int outline, Runnable onSelected) {
        super(owner);
        this.parent = parent;
        this.onSelected = onSelected;


        icon = new NavBarIcon(owner, fill, outline, NavBar.ICON_SIZE);

        setClipToPadding(false);

        addView(icon);

        setOnClickListener(e -> {
            if (onClick != null) {
                onClick.run();
            }
        });

        AlignUtils.alignInFrame(icon, Alignment.CENTER);

        setClickable(true);
        setFocusable(false);

        setOnClick(this::select);

        applyLocale(LocaleUtils.getLocale(owner));
    }

    public void setWidth(int width) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width,
                SizeUtils.dipToPx(NavBar.ITEM_HEIGHT, owner));
        setLayoutParams(p);
    }

    public NavBarItem(Context owner, NavBar navBar, Home home, @DrawableRes int fill, int outline,
                      Class<? extends HomePage> type) {
        this(owner, navBar, fill, outline, null);

        setOnSelected(() -> {
            HBox par = parent.getRoot();
            int ind = par.indexOfChild(this);
            int oldInd = selectedItem() == null ? -1 : par.indexOfChild(selectedItem());
            if(ind > oldInd) {
                home.nextInto(type, postOnSelected);
            }else {
                home.previousInto(type, postOnSelected);
            }
        });
    }

    private NavBarItem selectedItem() {
        return parent.getSelected();
    }

    private SelectedBack background() {
        return parent.getBack();
    }

    public boolean isCenter() {
        return this == parent.getHomeItem();
    }

    public boolean isRight() {
        int count = parent.getRoot().getChildCount();
        int index = parent.getRoot().indexOfChild(this);
        return index == count - 1;
    }

    public boolean isLeft() {
        int index = parent.getRoot().indexOfChild(this);
        return index == 0;
    }

    public void setOnSelected(Runnable onSelected) {
        this.onSelected = onSelected;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
        setFocusable(true);
    }

    private static ParallelAnimation selecting;

    public void select() {
        select(null);
    }

    public void select(Runnable post) {
        Platform.waitWhile(this::isNotLaidOut, () -> Platform.runBack(() -> {
            if(getVisibility() == INVISIBLE) return;
            if(selectedItem() == this) {
                if(post != null) post.run();
                return;
            }

            if(selecting != null && selecting.isRunning()) return;

            if(onSelected!= null) {
                if(post != null) {
                    postOnSelected = () -> {
                        post.run();
                        postOnSelected = null;
                    };
                }
                onSelected.run();
            }

            selecting = new ParallelAnimation(300)
                    .addAnimation(new ScaleXYAnimation(icon, 1.25f))
                    .addAnimation(new TranslateYAnimation(icon,
                            -SizeUtils.dipToPx(NavBar.ICON_SIZE * 0.75f, owner)))
                    .addAnimation(icon.fadeToSelected(true))
                    .setInterpolator(Interpolator.OVERSHOOT);

            SelectedBack back = background();

            int dx = calcX(this);
            float[] newCr = radius();
            int nw = getWidth();

            Style s = StyleUtils.getStyle(owner).get();
            StyleToColor newFill = isCenter() ? Style.BACK_TER : Style.EMPTY;
            int newColor = newFill.get(s);

            if (selectedItem() != null) {
                final NavBarItem old = selectedItem();

                StyleToColor oldFill = old.isCenter() ? Style.BACK_TER : Style.EMPTY;
                int oldColor = oldFill.get(s);

                int ow = old.getWidth();

                float[] oldCr = old.radius();

                selecting.addAnimation(new ScaleXYAnimation(old.icon, 1))
                        .addAnimation(new TranslateYAnimation(old.icon, 0))
                        .addAnimation(new TranslateXAnimation(back, dx))
                        .addAnimation(new WidthAnimation(back, nw))
                        .addAnimation(old.icon.fadeToSelected(false))
                        .addAnimation(new ColorAnimation(oldColor, newColor) {
                            @Override
                            public void updateValue(int color) {
                                back.setFill(color, newFill);
                            }
                        })
                        .addAnimation(new CornerRadiusAnimation(back, oldCr, newCr));

                parent.getBack().applyClip(
                        old.isCenter(),
                        isCenter(),
                        ow,
                        nw).start();
            } else {
                Platform.runLater(() -> {
                    back.setTranslationX(dx * LocaleUtils.getLocaleDirection(owner));
                    back.setAlpha(1);
                    back.getLayoutParams().width = nw;
                    back.setLayoutParams(back.getLayoutParams());
                    back.setFill(newFill);
                    back.setCornerRadius(newCr);
                    back.applyClip(isCenter(), nw);
                });
            }

            parent.setSelected(this);
            selecting.start();
        }));
    }

    private float[] radius;
    private float[] radius() {
        if(radius == null) {
            radius = isRight() ? CornerUtils.cornerBottomLeftRadius(owner, 30) :
                    isLeft() ? CornerUtils.cornerBottomRightRadius(owner, 30) :
                            isCenter() ? CornerUtils.cornerRadius(owner, 100) :
                                    CornerUtils.cornerBottomRadius(owner, 30);
        }
        return radius;
    }

    private int x = -1;
    private int calcX(View v1) {
        if(x == -1) {
            int r = 0;
            int ind = parent.getRoot().indexOfChild(v1);
            for(int i = 0; i < ind; i++) {
                r += parent.getRoot().getChildAt(i).getWidth();
            }
            x = r;
        }
        return x;
    }

    @Override
    public void applyLocale(Locale locale) {
        setScaleX(locale.isRtl() ? -1 : 1);
    }

}
