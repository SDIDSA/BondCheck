package com.sdidsa.bondcheck.app.app_content.session.content.main.posts;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;

import com.sdidsa.bondcheck.abs.UiCache;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredDateLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.DateFormat;
import com.sdidsa.bondcheck.abs.components.controls.text.DateLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredVBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemDetailsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.LocationOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.location.LocationDetail;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.LocationDisp;
import com.sdidsa.bondcheck.app.app_content.session.content.screenshots.ScreenshotOverlay;
import com.sdidsa.bondcheck.app.app_content.session.overlays.ViewProfileOverlay;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.PostDetail;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import java.util.HashMap;

public class PostDisplay extends ColoredVBox {
    private static final HashMap<String, PostDisplay> cache = new HashMap<>();
    public static PostDisplay make(Context owner, PostResponse post) {
        if(cache.containsKey(post.id())) {
            return cache.get(post.id());
        }
        PostDisplay display = new PostDisplay(owner, post);
        cache.put(post.id(), display);
        return display;
    }
    public static void clearCache() {
        cache.clear();
    }

    static {
        UiCache.register(PostDisplay::clearCache);
    }

    private static final int AVATAR_SIZE = 52;
    private final NetImage avatar;
    private final ColoredLabel username;

    private final DateLabel time;
    private final LocationDisp locationDisp;
    private final ColoredLabel detail;
    private final AppCompatTextView emoji;

    private final ColoredLabel content;

    public PostDisplay(Context owner) {
        this(owner, null);
    }

    private PostDisplay(Context owner, PostResponse post) {
        super(owner, Style.BACK_TER);
        setSpacing(15);
        setCornerRadius(20);
        setPadding(15);

        avatar = new NetImage(owner);
        avatar.setCornerRadius(AVATAR_SIZE);
        avatar.setSize(AVATAR_SIZE);

        username = new ColoredLabel(owner, Style.TEXT_SEC, "")
                .setFont(new Font(20, FontWeight.MEDIUM));
        detail = new ColoredLabel(owner, Style.TEXT_MUT, "")
                .setFont(new Font(18));
        emoji = new AppCompatTextView(owner);
        Label.setFont(emoji, new Font(20));
        MarginUtils.setMarginHorizontal(detail, owner, 7);
        locationDisp = new LocationDisp(owner);
        locationDisp.setLayoutParams(new LayoutParams(-2, SizeUtils.dipToPx(42, owner)));

        time = new ColoredDateLabel(owner, Style.TEXT_SEC);
        time.setFont(new Font(16));
        time.setFormat(DateFormat.RELATIVE);
        time.setAlpha(.6f);

        content = new ColoredLabel(owner, Style.TEXT_NORM, "")
                .setFont(new Font(18));
        //MarginUtils.setMarginTop(content, owner, 7);

        HBox preUsername = new HBox(owner);
        preUsername.setAlignment(Alignment.CENTER_LEFT);

        VBox topRight = new VBox(owner);
        SpacerUtils.spacer(topRight, Orientation.HORIZONTAL);
        MarginUtils.setMarginLeft(topRight, owner, 15);

        preUsername.addView(username);
        preUsername.addView(detail);
        preUsername.addView(emoji);
        topRight.addView(preUsername);
        topRight.addView(time);

        HBox top = new HBox(owner);
        top.addView(avatar);
        top.addView(topRight);

        addView(top);

        if(post != null) load(post);
    }

    private void load(PostResponse post) {
        setOnClickListener(e -> ItemDetailsOverlay.getInstance(owner).show(post));

        avatar.startLoading();

        LocationDetail location = post.getLocationDetail();
        if(location != null) {
            locationDisp.load(location);
            addView(locationDisp, 0);
            locationDisp.setOnClick(() ->
                    LocationOverlay.getInstance(owner).show(post));
        }
        PostDetail detail = post.getPostDetail();
        if(detail != null) {
            this.detail.setKey(detail.description());
            emoji.setText(detail.emoji());
        }

        SessionService.getUser(owner, post.by(), user -> {
            avatar.bindToUser(user);
            avatar.setOnClick(() -> {
                ViewProfileOverlay.getInstance(owner).show(post.by());
            });
            username.setText(user.getUsername());
        });

        time.setDate(post.created_at());

        String cont = post.content();
        if(cont != null && !cont.equals("null")) {
            content.setText(cont);
            addView(content);
        }

        prepareImages(post);
    }

    private void prepareImages(PostResponse post) {
        String[] media = post.getMedia();
        if(media != null && media.length > 0) {
            int wpx = ContextUtils.getScreenWidth(owner);
            float wdp = SizeUtils.pxToDip(wpx, owner);
            float size = wdp - 60;
            if(media.length == 1) {
                NetImage img = prepareImage(post, media[0], size, size);
                img.setCornerRadius(15);
                addView(img);
            } else if(media.length == 2) {
                HBox images = new HBox(owner);
                images.setCornerRadius(15);
                images.setClipToOutline(true);
                float widthDp = size / 2 - 1;
                int widthPx = SizeUtils.dipToPx(widthDp, owner);
                int heightPx = (widthPx * 3) / 2;
                float heightDp = SizeUtils.pxToDip(heightPx, owner);
                for(int i = 0; i < media.length; i++) {
                    String m = media[i];
                    NetImage img = prepareImage(post, m, widthDp, heightDp);
                    images.addView(img);
                    if(i == 1) {
                        MarginUtils.setMarginLeft(img, owner, 2);
                    }
                }
                addView(images);
            } else {
                HBox images = new ColoredHBox(owner, Style.BACK_TER);
                images.setClipToOutline(true);
                images.setCornerRadius(15);
                VBox right = new VBox(owner);
                right.setSpacing(2);
                MarginUtils.setMarginLeft(right, owner, 2);

                images.addView(right);

                float leftWidthDp = ((size * 3) / 5) - 1;
                float leftHeightDp = ((size * 4) / 5) - 1;
                float rightWidthDp = ((size * 2) / 5) - 1;
                float rightHeightDp = ((size * 2) / 5) - 1;

                for(int i = 0; i < 3; i++) {
                    String m = media[i];
                    if(i == 0) {
                        NetImage img = prepareImage(post, m, leftWidthDp, leftHeightDp);
                        images.addView(img, 0);
                    } else {
                        NetImage img = prepareImage(post, m, rightWidthDp, rightHeightDp);
                        right.addView(img);
                    }
                }
                if(media.length > 3) {
                    int diff = media.length - 2;
                    ColoredLabel diffLabel = new ColoredLabel(owner, Style.TEXT_NORM, "")
                            .setFont(new Font(24, FontWeight.MEDIUM));
                    diffLabel.setText("+".concat(String.valueOf(diff)));
                    NetImage lastImage = (NetImage) right.getChildAt(1);
                    lastImage.setViewAlpha(.4f);
                    lastImage.addAligned(diffLabel, Alignment.CENTER);
                }

                addView(images);
            }
        }
    }

    private NetImage prepareImage(PostResponse post, String url, Float wdp, Float hdp) {
        NetImage img = new NetImage(owner, Style.BACK_PRI);

        img.setOnClick(() -> ScreenshotOverlay.getInstance(owner).show(post, url));

        img.setImageThumbUrl(url, wdp, hdp);
        return img;
    }
}
