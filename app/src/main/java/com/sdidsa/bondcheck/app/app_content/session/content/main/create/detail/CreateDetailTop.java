package com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail;

import android.content.Context;
import android.view.View;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateXAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.CreateMain;

public class CreateDetailTop extends HBox {
    private final CreateDetail parent;
    private final StackPane labels;
    private final ColoredLabel feelingRight;
    private final ColoredLabel feelingLeft;
    private final ColoredLabel activityRight;
    private final ColoredLabel activityLeft;

    private boolean isAtFeeling = true;

    public CreateDetailTop(Context owner) {
        this(owner, Fragment.getInstance(owner, CreateDetail.class));
    }

    public CreateDetailTop(Context owner, CreateDetail parent) {
        super(owner);
        this.parent = parent;
        setAlignment(Alignment.CENTER);

        ColorIcon back = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.arrow_left, 48);
        back.setAutoMirror(true);
        back.setPadding(12);
        back.setOnClick(() -> parent.getPane().previousInto(CreateMain.class));
        MarginUtils.setMarginRight(back, owner, 10);

        feelingRight = new ColoredLabel(owner, Style.TEXT_NORM, "feeling_header")
                .setFont(new Font(22, FontWeight.MEDIUM));
        feelingRight.setPadding(10);

        feelingLeft = new ColoredLabel(owner, Style.TEXT_NORM, "feeling_header")
                .setFont(new Font(22, FontWeight.MEDIUM));
        feelingLeft.setPadding(10);

        activityRight = new ColoredLabel(owner, Style.TEXT_NORM, "activity_header")
                .setFont(new Font(22, FontWeight.MEDIUM));
        activityRight.setAlpha(.5f);
        activityRight.setPadding(10);

        activityLeft = new ColoredLabel(owner, Style.TEXT_NORM, "activity_header")
                .setFont(new Font(22, FontWeight.MEDIUM));
        activityLeft.setAlpha(0);
        activityLeft.setPadding(10);

        feelingRight.setOnClickListener(e -> switchToFeeling());
        feelingRight.setAlpha(0);

        activityRight.setOnClickListener(e -> switchToActivity());

        labels = new StackPane(owner);

        labels.addAligned(feelingRight, Alignment.CENTER_RIGHT);
        labels.addAligned(activityRight, Alignment.CENTER_RIGHT);

        labels.addAligned(feelingLeft, Alignment.CENTER_LEFT);
        labels.addAligned(activityLeft, Alignment.CENTER_LEFT);

        SpacerUtils.spacer(labels);

        addView(back);
        addView(labels);
    }

    private Animation running;

    private void switchToActivity() {
        if(running != null && running.isRunning()) return;
        if(!isAtFeeling) return;
        isAtFeeling = false;

        parent.getContent().nextInto(ActivityFragment.class);

        int tx = getTargetX(activityLeft);

        activityLeft.setTranslationX(tx);
        activityLeft.setAlpha(0.5f);
        activityRight.setAlpha(0);
        activityRight.setClickable(false);
        feelingRight.setClickable(true);

        running = new ParallelAnimation(300)
                .addAnimation(new TranslateXAnimation(activityLeft, tx, 0))
                .addAnimation(Animation.fadeOutLeft(owner, feelingLeft))
                .addAnimation(Animation.fadeInLeft(owner, feelingRight, .5f))
                .addAnimation(new AlphaAnimation(activityLeft, .5f, 1))
                .setInterpolator(Interpolator.OVERSHOOT)
                .start();
    }

    private void switchToFeeling() {
        if(running != null && running.isRunning()) return;
        if(isAtFeeling) return;
        isAtFeeling = true;

        parent.getContent().nextInto(FeelingFragment.class);

        int tx = getTargetX(feelingLeft);

        feelingLeft.setTranslationX(tx);
        feelingLeft.setAlpha(0.5f);
        feelingRight.setAlpha(0);
        feelingRight.setClickable(false);
        activityRight.setClickable(true);

        running = new ParallelAnimation(300)
                .addAnimation(new TranslateXAnimation(feelingLeft, tx, 0))
                .addAnimation(Animation.fadeOutLeft(owner, activityLeft))
                .addAnimation(Animation.fadeInLeft(owner, activityRight, .5f))
                .addAnimation(new AlphaAnimation(feelingLeft, .5f, 1))
                .setInterpolator(Interpolator.OVERSHOOT)
                .start();
    }

    private int getScreenX(View view) {
        int[] loc = new int[2];
        ContextUtils.getLocationOnScreen(view, loc);
        return loc[0];
    }

    private int getTargetX(View view) {
        return getScreenX(this) + getWidth() - getPaddingRight() - view.getWidth() - getScreenX(labels);
    }
}
