package com.sdidsa.bondcheck.app.app_content.session.content.main.create.main;

import android.content.Context;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.WidthAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.input.MinimalInputField;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.abs.CornerUtils;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.CreateFragment;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail.ActivityFragment;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail.CreateDetail;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail.FeelingFragment;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.location.LocationDetail;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.location.LocationFragment;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.media.PostMedia;
import com.sdidsa.bondcheck.models.PostDetail;

public class CreateMain extends CreateFragment {
    private final MinimalInputField head;
    private final PostMedia images;
    private PostDetail detail;
    private LocationDetail location;

    private final DetailDisp detailDisp;

    private final LocationDisp locationDisp;

    private final VBox preBottom;

    private final ParallelAnimation show;

    public CreateMain(Context owner) {
        super(owner);
        setSpacing(0);
        setAlignment(Alignment.TOP_RIGHT);

        HBox top = new HBox(owner);
        top.setAlignment(Alignment.TOP_RIGHT);
        top.setCornerRadius(CornerUtils.cornerTopRadius(owner, 20));
        SpacerUtils.spacer(top, Orientation.VERTICAL);

        head = new MinimalInputField(owner, "create_post_hint");
        head.setMultiline(-1);
        head.setBackFill(Style.EMPTY);
        head.setTextFill(Style.TEXT_NORM);
        head.setFont(new Font(18));
        head.setPadding(7);
        head.setLineSpacing(5);
        SpacerUtils.spacer(head);
        head.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
        head.setLayoutParams(head.getLayoutParams());

        ColoredIcon createIcon = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.create, 56);
        createIcon.setPadding(14);
        createIcon.setAlpha(.5f);
        top.addViews(head, createIcon);

        HBox bottom = new HBox(owner);
        bottom.setAlignment(Alignment.BOTTOM_RIGHT);
        bottom.setCornerRadius(CornerUtils.cornerBottomRadius(owner, 20));
        PaddingUtils.setPadding(bottom, 15, 0, 15, 15, owner);

        CreateIcon media = new CreateIcon(owner, R.drawable.photo_video);
        CreateIcon feeling = new CreateIcon(owner, R.drawable.smiling);
        CreateIcon location = new CreateIcon(owner, R.drawable.location_fill);

        MarginUtils.setMarginRight(feeling, owner, 10);
        MarginUtils.setMarginRight(location, owner, 10);

        preBottom = new VBox(owner);
        preBottom.setSpacing(10);

        images = new PostMedia(owner);

        detailDisp = new DetailDisp(owner);
        detailDisp.setOnClick(() -> getPane().nextInto(CreateDetail.class));
        detailDisp.setOnRemove(() -> {
            this.detail = null;
            Animation.fadeOutLeft(owner, detailDisp)
                    .setInterpolator(Interpolator.EASE_OUT)
                    .setOnFinished(() -> preBottom.removeView(detailDisp))
                    .start();
        });

        locationDisp = new LocationDisp(owner);
        locationDisp.setOnClick(() -> getPane().nextInto(LocationFragment.class));
        locationDisp.setOnRemove(() -> {
            this.location = null;
            Animation.fadeOutLeft(owner, locationDisp)
                    .setInterpolator(Interpolator.EASE_OUT)
                    .setOnFinished(() -> preBottom.removeView(locationDisp))
                    .start();
        });

        media.setOnClickListener(e -> images.add());
        feeling.setOnClickListener(e -> getPane().nextInto(CreateDetail.class));
        location.setOnClickListener(e -> getPane().nextInto(LocationFragment.class));

        PostButton post = new PostButton(owner, this);

        show = new ParallelAnimation(300)
                .addAnimation(new WidthAnimation(post, 0,
                        SizeUtils.dipToPx(72, owner)))
                .addAnimation(new AlphaAnimation(post, 0, 1));

        preBottom.addViews(images, bottom);
        bottom.addViews(location, feeling, media, post);

        addViews(top, preBottom);
    }

    public void onDetail(PostDetail detail) {
        if (this.detail == null) {
            preBottom.addView(detailDisp, 1);
        }
        this.detail = detail;
        detailDisp.load(detail);
        getPane().previousInto(CreateMain.class);
    }

    public void onLocation(LocationDetail location) {
        if (this.location == null) {
            preBottom.addView(locationDisp, detail == null ? 1 : 2);
        }
        this.location = location;
        locationDisp.load(location);
        getPane().previousInto(CreateMain.class);
    }

    public ParallelAnimation getShow() {
        return show;
    }

    public MinimalInputField getHead() {
        return head;
    }

    public PostMedia getImages() {
        return images;
    }

    public LocationDetail getLocation() {
        return location;
    }

    public PostDetail getDetail() {
        return detail;
    }

    public void reset() {
        locationDisp.getOnRemove().run();
        detailDisp.getOnRemove().run();
        images.reset();
        head.setValue("");
        Fragment.getInstance(owner, FeelingFragment.class).reset();
        Fragment.getInstance(owner, ActivityFragment.class).reset();
        Fragment.getInstance(owner, LocationFragment.class).reset();
    }
}
