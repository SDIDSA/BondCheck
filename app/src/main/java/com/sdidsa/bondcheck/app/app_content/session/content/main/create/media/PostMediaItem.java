package com.sdidsa.bondcheck.app.app_content.session.content.main.create.media;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.WidthAnimation;
import com.sdidsa.bondcheck.abs.animation.view.margin.MarginLeftAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.Image;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.data.media.Media;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

public class PostMediaItem extends StackPane {
    public PostMediaItem(Context owner) {
        this(owner, null);
    }

    public PostMediaItem(Context owner, Media media) {
        super(owner);

        Image img = new Image(owner);
        img.setSize(80);
        img.setCornerRadius(10);
        img.setAlpha(.4f);

        if(media != null)
            media.getThumbnail(owner,
                SizeUtils.dipToPx(80, owner),
                img::setImageBitmap,
                () -> ContextUtils.toast(owner, "problem_string"));

        ColoredIcon remove = new ColoredIcon(owner, Style.TEXT_NORM, R.drawable.close, 42);
        remove.setPadding(15);
        remove.setRadiusNoClip(48);
        remove.setBorder(Style.TEXT_NORM, 2);

        addAligned(img, Alignment.CENTER);
        addAligned(remove, Alignment.CENTER);
    }

    public void hide(Runnable post) {
        boolean last = ((HBox) getParent()).getChildCount() == 1;
        new ParallelAnimation(300)
                .addAnimation(!last ?
                        new WidthAnimation(this, getWidth(), 0) :
                        new MarginLeftAnimation(this, 0))
                .addAnimation(new AlphaAnimation(this, 1, 0))
                .addAnimation(new MarginLeftAnimation(this, 0))
                .setOnFinished(post)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }
}
