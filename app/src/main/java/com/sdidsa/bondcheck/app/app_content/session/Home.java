package com.sdidsa.bondcheck.app.app_content.session;

import android.content.Context;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.fragment.FragmentPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.MultipleOptionOverlay;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;
import com.sdidsa.bondcheck.app.app_content.session.content.history.History;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Items;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.navbar.NavBar;
import com.sdidsa.bondcheck.app.app_content.session.overlays.HomeExitOverlay;

public class Home extends Page {
    private final NavBar navBar;

    private final FragmentPane content;

    public Home(Context owner) {
        super(owner);

        content = new FragmentPane(owner, HomePage.class);
        ContextUtils.spacer(content);

        navBar = new NavBar(owner, this);

        VBox root = new VBox(owner);
        root.addViews(content,
                navBar);

        addView(root);
    }

    public NavBar getNavBar() {
        return navBar;
    }

    public void nextInto(Class<? extends HomePage> pageType, Runnable post) {
        content.nextInto(pageType, post);
    }

    public void previousInto(Class<? extends HomePage> pageType, Runnable post) {
        content.previousInto(pageType, post);
    }

    @Override
    public Animation setup(int direction) {
        if(setup == null) {
            setup = new ParallelAnimation(400)
                    .addAnimation(Animation.fadeInUp(owner, navBar))
                    .addAnimation(Animation.fadeInDown(owner, content))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        content.setAlpha(0);
        navBar.setAlpha(0);
        return setup;
    }

    @Override
    public Animation destroy(int direction) {
        if(destroy == null) {
            destroy = new ParallelAnimation(400)
                    .addAnimation(Animation.fadeOutDown(owner, navBar))
                    .addAnimation(Animation.fadeOutUp(owner, content))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        return destroy;
    }

    private MultipleOptionOverlay confirmExit;
    @Override
    public boolean onBack() {
        if(content.getLoaded() instanceof Items) {
            previousInto(History.class, null);
            return true;
        }

        if(!(content.getLoaded() instanceof Main)) {
            navBar.getHomeItem().select();
            return true;
        }

        exit();

        return true;
    }

    public void exit() {
        if(confirmExit == null)
            confirmExit = new HomeExitOverlay(owner);

        confirmExit.show();
    }

    @Override
    public void applyInsets(Insets insets) {
        if(insets== null || (insets.top + insets.bottom > ContextUtils.getScreenHeight(owner) / 4)) return;
        setPadding(insets.left, 0, insets.right, 0);
        navBar.setPadding(0,0, 0, insets.bottom);
        Fragment loaded = content.getLoaded();
        if(loaded instanceof HomePage page) {
            page.applyInsets(insets);
        }
    }
}
