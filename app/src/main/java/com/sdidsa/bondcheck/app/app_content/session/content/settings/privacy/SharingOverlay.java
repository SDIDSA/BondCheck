package com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy;

import android.content.Context;
import android.view.Gravity;

import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.LinearHeightAnimation;
import com.sdidsa.bondcheck.abs.components.controls.input.toggle.LabeledToggle;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.BroadcastListener;
import com.sdidsa.bondcheck.app.services.SocketService;

public class SharingOverlay extends PartialSlideOverlay implements Styleable {
    protected final VBox root;
    private final Label text;

    private BroadcastListener listener;

    public SharingOverlay(Context owner) {
        this(owner, "Header text");
    }

    private int modularHeight = -1;
    public SharingOverlay(Context owner, String header) {
        super(owner, -2);

        list.setPadding(20);
        list.setSpacing(30);
        list.setGravity(Gravity.TOP | Gravity.CENTER);

        text = new Label(owner, header);
        text.centerText();
        text.setFont(new Font(20, FontWeight.MEDIUM));
        text.setLineSpacing(10);

        root = new VBox(owner);
        root.setSpacing(20);
        root.setAlignment(Alignment.CENTER);
        SpacerUtils.spacer(root, Orientation.VERTICAL);

        VBox modular = new VBox(owner);
        modular.setAlignment(Alignment.TOP_CENTER);
        modular.setSpacing(20);
        modular.setClipToPadding(true);
        modular.setPadding(10);

        LabeledToggle pauseAll = new LabeledToggle(owner, "pause_all");
        pauseAll.setFont(new Font(22));
        pauseAll.getToggle().setSize(28);

        pauseAll.getToggle().setOnChange(b -> {
            SocketService.setPaused(owner, !b);
            if(b) {
                new ParallelAnimation(400)
                        .addAnimation(new LinearHeightAnimation(modular, modularHeight, 0))
                        .addAnimation(new AlphaAnimation(modular, 0))
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();
            } else {
                new ParallelAnimation(400)
                        .addAnimation(new LinearHeightAnimation(modular, 0, modularHeight))
                        .addAnimation(new AlphaAnimation(modular, 1))
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();
            }
        });

        addOnShowing(() -> {
            Platform.waitWhileNot(modular::isLaidOut, () -> {
                if(modularHeight == -1) modularHeight = modular.getHeight();
                pauseAll.setEnabled(Store.isPauseSharing());
            });
            listener = new BroadcastListener(owner);
            listener.on(Action.PAUSE_SHARING, () -> pauseAll.setEnabled(true));
            listener.on(Action.RESUME_SHARING, () -> pauseAll.setEnabled(false));
        });

        LabeledToggle micSharing = new LabeledToggle(owner, "microphone_sharing");
        micSharing.setFont(new Font(18));
        LabeledToggle screenSharing = new LabeledToggle(owner, "screen_sharing");
        screenSharing.setFont(new Font(18));
        LabeledToggle locSharing = new LabeledToggle(owner, "location_sharing");
        locSharing.setFont(new Font(18));

        micSharing.setEnabled(Store.isEnableMic());
        screenSharing.setEnabled(Store.isEnableScreen());
        locSharing.setEnabled(Store.isEnableLocation());

        modular.addViews(micSharing, screenSharing, locSharing);

        root.addView(pauseAll);
        root.addView(modular);

        list.addView(text);
        list.addView(root);

        addOnShowing(() -> applyStyle(StyleUtils.getStyle(owner).get()));

        applyStyle(StyleUtils.getStyle(owner));
    }

    @Override
    public void applyStyle(Style style) {
        text.setFill(style.getTextNormal());
    }

}
