package com.sdidsa.bondcheck.app.app_content.session.content.history;

import android.content.Context;
import android.util.Log;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Refresh;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;
import com.sdidsa.bondcheck.app.app_content.session.content.history.location.LocationHistory;
import com.sdidsa.bondcheck.app.app_content.session.content.history.microphone.MicrophoneHistory;
import com.sdidsa.bondcheck.app.app_content.session.content.history.screen.ScreenHistory;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.Locations;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondState;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondStatus;
import com.sdidsa.bondcheck.app.app_content.session.content.records.Records;
import com.sdidsa.bondcheck.app.app_content.session.content.screenshots.Screenshots;
import com.sdidsa.bondcheck.app.app_content.session.navbar.NavBar;

public class History extends HomePage {

    protected final Refresh refresh;
    private final VBox histories;

    private final ScreenHistory screens;
    private final MicrophoneHistory records;
    private final LocationHistory locations;

    private Animation showOther;
    private Animation hideOther;

    public History(Context owner) {
        super(owner, "History");
        ContextUtils.setPadding(content, 15, 0, 15, 15, owner);

        histories = new VBox(owner);
        histories.setSpacing(10);
        histories.setAlpha(0);

        screens = new ScreenHistory(owner);
        records = new MicrophoneHistory(owner);
        locations = new LocationHistory(owner);

        refresh = new Refresh(owner);
        refresh.setOnClick(() -> {
            refresh.hide().start();
            Platform.runBack(() -> {
                screens.fetch();
                records.fetch();
                locations.fetch();
                Platform.waitWhile(() -> !screens.isReady() &&
                        !records.isReady() &&
                        !locations.isReady(), () -> refresh.show().start());
            });
        });
        top.addView(refresh);

        scrollable.setOnMaybeRefresh(this::onMaybeRefresh);
        scrollable.setOnRefresh(this::onRefresh);

        content.addView(histories);
    }

    public void onMaybeRefresh(float dist) {
        refresh.applyRefresh(dist);
    }

    public void onRefresh() {
        refresh.fire();
    }

    @Override
    public void setup(boolean direction) {
        super.setup(direction);

        if(records.isReady() && locations.isReady() && screens.isReady()) {
            Animation an = Animation.sequenceFadeInUp(owner, ContextUtils.getViewChildren(histories));
            an.start();
        }
    }

    public void init(BondStatus bondStatus) {

        screens.otherUser().bind(bondStatus.otherUser());
        records.otherUser().bind(bondStatus.otherUser());
        locations.otherUser().bind(bondStatus.otherUser());

        bondStatus.bondStateProperty().addListener((ov, nv) ->
                Platform.runBack(() -> {
                    boolean visibility = nv == BondState.BOND_ACTIVE;
                    boolean oldVis = ov == BondState.BOND_ACTIVE;

                    if(visibility && !oldVis) {
                        if(showOther == null) {
                            Home home = Page.getInstance(owner, Home.class);
                            assert home != null;
                            NavBar navBar = home.getNavBar();
                            showOther = new ParallelAnimation(300)
                                    .addAnimation(Animation
                                            .scaleUpIn(screens, records, locations))
                                    .addAnimation(Animation.fadeIn(histories))
                                    .addAnimation(navBar.showBond())
                                    .setInterpolator(Interpolator.OVERSHOOT);
                        }
                        Platform.runLater(() -> {
                            histories.addView(screens);
                            histories.addView(records);
                            histories.addView(locations);
                        });
                        if(hideOther != null && hideOther.isRunning()) {
                            hideOther.stop();
                        }
                        showOther.start();
                    }else if(oldVis && !visibility) {
                        Home home = Page.getInstance(owner, Home.class);
                        assert home != null;
                        NavBar navBar = home.getNavBar();

                        navBar.getHomeItem().select();

                        Fragment.clearCache(Records.class);
                        Fragment.clearCache(Locations.class);
                        Fragment.clearCache(Screenshots.class);

                        if(hideOther == null) {
                            hideOther =  new ParallelAnimation(300)
                                    .addAnimation(Animation
                                            .scaleDownOut(screens, records, locations))
                                    .addAnimation(Animation.fadeOut(histories))
                                    .addAnimation(navBar.hideBond())
                                    .setInterpolator(Interpolator.EASE_OUT)
                                    .setOnFinished(() -> {
                                        histories.removeView(screens);
                                        histories.removeView(records);
                                        histories.removeView(locations);
                                    });
                        }
                        if(showOther != null && showOther.isRunning()) {
                            showOther.stop();
                        }
                        hideOther.start();
                    }

                    if(visibility) {
                        screens.fetch();
                        records.fetch();
                        locations.fetch();
                    }
                }));
    }
}
