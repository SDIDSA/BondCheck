package com.sdidsa.bondcheck.app.app_content.session.content.main.shared;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredLinearLoading;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.Loading;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;

public class HomeSection extends StackPane {
    public static final int ITEM_SIZE = 84;

    private final VBox content;
    private final StackPane preContent;

    private final Loading loading;

    public HomeSection(Context owner) {
        this(owner, "Section Title", -1);
    }

    public HomeSection(Context owner, String titleText, @DrawableRes int iconRes) {
        super(owner);

        ContextUtils.setPaddingTop(this, 20, owner);
        setClipToPadding(false);

        HBox preTitle = new ColoredHBox(owner, Style.BACK_PRI);
        preTitle.setLayoutParams(new LayoutParams(-2, -2));
        preTitle.setAlignment(Alignment.CENTER);
        preTitle.setTranslationY(-ContextUtils.dipToPx(20, owner));
        ContextUtils.setMarginLeft(preTitle, owner, 20);

        ColorIcon icon = new ColoredIcon(owner, Style.TEXT_NORM, iconRes, 18);
        ContextUtils.setMarginRight(icon, owner, 10);

        Label titleLabel = new ColoredLabel(owner, Style.TEXT_NORM, titleText);
        preTitle.setCornerRadius(10);
        titleLabel.setFont(new Font(18, FontWeight.MEDIUM));
        ContextUtils.setPaddingHorizontalVertical(preTitle, 10, 6, owner);
        preTitle.addViews(icon, titleLabel);

        content = new VBox(owner);
        content.setSpacing(15);


        preContent = new ColoredStackPane(owner, Style.BACK_TER);
        preContent.setCornerRadius(15);
        preContent.addView(content);
        preContent.setClipToPadding(false);
        preContent.setClipChildren(false);

        ContextUtils.setPadding(preContent, 15, 22, 15, 15, owner);
        addView(preContent);
        addView(preTitle);

        loading = new ColoredLinearLoading(owner, Style.TEXT_SEC, 12);
        ContextUtils.setPaddingTop(loading.getView(), 5, owner);
        loading.getView().setAlpha(0f);
    }

    private Animation running;
    private Animation startLoading;
    public void startLoading() {
        if(running != null && running.isRunning()) {
            running.stop();
        }
        loading.getView().setAlpha(0f);
        Platform.runLater(() -> {
            preContent.removeView(loading.getView());
            preContent.addCentered(loading.getView());
        });
        loading.startLoading();
        if(startLoading == null) {
            startLoading = new ParallelAnimation(300)
                    .addAnimations(ParallelAnimation.fadeOutUp(owner, content))
                    .addAnimations(ParallelAnimation.fadeInUp(owner, loading.getView()))
                    .setInterpolator(Interpolator.OVERSHOOT);
        }
        running = startLoading;
        running.start();
    }

    private Animation stopLoading;
    public void stopLoading() {
        if(running != null && running.isRunning()) {
            running.stop();
        }
        if(stopLoading == null) {
            stopLoading = new ParallelAnimation(300)
                    .addAnimations(ParallelAnimation.fadeInUp(owner, content))
                    .addAnimations(ParallelAnimation.fadeOutUp(owner, loading.getView()))
                    .setInterpolator(Interpolator.OVERSHOOT)
                    .setOnFinished(() -> {
                        loading.stopLoading();
                        preContent.removeView(loading.getView());
                    });
        }
        running = stopLoading;
        running.start();
    }

    public void addToContent(View view) {
        content.addView(view);
    }
}
