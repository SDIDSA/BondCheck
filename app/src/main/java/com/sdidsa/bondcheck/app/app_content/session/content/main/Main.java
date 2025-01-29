package com.sdidsa.bondcheck.app.app_content.session.content.main;

import android.content.Context;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.Loading;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.scroll.Scroller;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;
import com.sdidsa.bondcheck.app.app_content.session.content.history.History;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondState;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondStatus;
import com.sdidsa.bondcheck.app.app_content.session.content.main.posts.PostDisplay;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.overlays.CreateOverlay;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.Gender;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class Main extends HomePage {
    private final NetImage img;
    private final BondStatus bondStatus;
    private CreateMock createMock;
    private CreateOverlay createOverlay;

    private VBox feedContent;
    private final StickyCreate stickyCreate;
    private Scroller feed;
    private final HBox sticky;

    public Main(Context owner) {
        super(owner, "home");

        PaddingUtils.setPadding(content, 15, 0, 15, 15, owner);
        content.setSpacing(0);

        img = new NetImage(owner);
        img.setSize(48);
        img.setCornerRadius(48);
        img.setPadding(5);

        top.addView(img);

        sticky = new HBox(owner);
        sticky.setAlpha(0);
        sticky.setLayoutParams(new LayoutParams(-1, -2));
        sticky.setAlignment(Alignment.CENTER);
        sticky.setClipChildren(false);
        sticky.setClipToPadding(false);

        stickyCreate = new StickyCreate(owner);
        stickyCreate.setClipChildren(false);
        stickyCreate.setClipToPadding(false);


        bondStatus = new BondStatus(owner);
        SpacerUtils.spacer(bondStatus);
        sticky.addViews(bondStatus);

        top.addView(stickyCreate, 2);

        img.setOnClick(() -> {
            Settings settings = Fragment.getInstance(owner, Settings.class);
            assert settings != null;
            settings.getAccountGroup().showUserProfile();
        });

        img.startLoading();
        SessionService.getUser(owner, Store.getUserId(), resp -> {
            resp.avatar().addListener((ov, nv) ->
            {
                if(nv != null) {
                    ImageProxy.getImageThumb(owner, nv, SizeUtils.dipToPx(48, owner),
                            img::setImageBitmap);
                }else {
                    Gender g = resp.genderValue();
                    if(g == Gender.Female) {
                        img.setImageResource(R.drawable.avatar_female);
                    }else {
                        img.setImageResource(R.drawable.avatar_male);
                    }
                }
            });

            resp.gender().addListener((ov, nv) -> {
                if(resp.getAvatar() == null) {
                    if(resp.genderValue() == Gender.Female) {
                        img.setImageResource(R.drawable.avatar_female);
                    }else {
                        img.setImageResource(R.drawable.avatar_male);
                    }
                }

            });
        });
        stickyCreate.setOnClickListener(e -> showCreateOverlay(stickyCreate));

        Fragment.getInstance(owner, History.class).init(bondStatus);

        bondStatus.fetch();

        bondStatus.bondStateProperty().addListener((ov ,v) -> {
            if(v == BondState.BOND_ACTIVE && ov != BondState.BOND_ACTIVE) {
                if(feed == null) {
                    createFeed();
                }
                refreshFeed();
                feed.setAlpha(0f);
                feed.setTranslationY(0);
                feed.setTranslationX(0);
                feed.setScaleX(1);
                feed.setScaleY(1);

                Animation.fadeInRight(owner, feed)
                        .setDuration(300)
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();
            }else if(v != BondState.BOND_ACTIVE && ov == BondState.BOND_ACTIVE){
                Animation.fadeOut(feed)
                        .setDuration(300)
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();
            }
        });

        content.addView(sticky);

        content.setClipChildren(false);
    }

    private void createFeed() {
        feedContent = new VBox(owner);
        feedContent.setSpacing(15);
        feedContent.setAlignment(Alignment.TOP_CENTER);


        createMock = new CreateMock(owner);
        createMock.setLayoutParams(new LayoutParams(-1, -2));

        createMock.setOnClickListener(e -> showCreateOverlay(createMock));

        feed = new Scroller(owner);

        feed.setContent(feedContent);
        feed.setOnRefresh(this::onRefresh);
        feed.setFill(Style.BACK_PRI);
        feed.setCornerRadius(20);
        feed.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        feed.setClipToOutline(true);
        feed.setBackground(feed.getBackground());
        int heightOpen = SizeUtils.dipToPx(160, owner);
        feed.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(scrollY > heightOpen && scrollY > oldScrollY) {
                bondStatus.collapse();
                stickyCreate.show();
            }else if(scrollY < heightOpen && scrollY < oldScrollY) {
                stickyCreate.hide();
            }
        });
        MarginUtils.setMarginTop(feed, owner, 15);

        root.removeView(scrollable);
        scrollable.removeView(content);
        root.addView(content);
        content.setClipChildren(true);

        feed.setAlpha(0f);

        feed.setOnRefreshGesture(this::onRefreshGesture);
        content.addView(feed);
    }

    private Loading loadingPosts;
    private void refreshFeed() {
        feedContent.removeAllViews();
        feedContent.addView(createMock);

        if(loadingPosts == null) {
            loadingPosts = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);
            MarginUtils.setMarginTop(loadingPosts.getView(), owner, 30);
        }
        loadingPosts.startLoading();
        feedContent.addView(loadingPosts.getView());

        Call<List<PostResponse>> call = App.api(owner).getPosts(
                new StringRequest(bondStatus.getOther_user()));

        Service.enqueue(call, resp -> {
            if(resp.isSuccessful()) {
                Platform.runBack(() -> {
                    List<PostDisplay> contents = new ArrayList<>();
                    assert resp.body() != null;
                    for(PostResponse post : resp.body()) {
                        PostDisplay content = PostDisplay.make(owner, post);
                        content.setAlpha(0);
                        contents.add(content);
                    }
                    Platform.runLater(() -> {
                        feedContent.removeView(loadingPosts.getView());
                        loadingPosts.stopLoading();
                        contents.forEach(feedContent::addView);
                        Animation.sequenceFadeInUp(owner, contents.toArray(new PostDisplay[0])).start();
                    });
                });

            } else {
                ContextUtils.toast(owner, "failed to load posts");
            }
        });
    }

    public void addPost(PostResponse post) {
        PostDisplay content = PostDisplay.make(owner, post);
        content.setAlpha(0);
        feedContent.addView(content, 1);
        Animation.fadeInRight(owner, content).setInterpolator(Interpolator.EASE_OUT).start();
    }

    private void showCreateOverlay(View view) {
        if(createOverlay == null) {
            createOverlay = new CreateOverlay(owner);
        }

        createOverlay.show(view);
    }

    private void onRefreshGesture(float dist) {
        createMock.setRefreshProgress(dist);
    }

    private void onRefresh() {
        bondStatus.fetch();
        new Animation() {
            @Override
            public void update(float v) {
                onRefreshGesture(1 - v);
            }
        }
                .setDuration(300)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    @Override
    public void setup(boolean direction) {
        super.setup(direction);

        if(bondStatus.getBondState() == BondState.BOND_ACTIVE) {
            Animation.sequenceFadeInUp(owner, ContextUtils.getViewChildren(content))
                    .start();
        }else {
            Animation.sequenceFadeInUp(owner, sticky).start();
        }
    }

    public BondStatus getBondStatus() {
        return bondStatus;
    }

    public CreateOverlay getCreateOverlay() {
        return createOverlay;
    }
}
