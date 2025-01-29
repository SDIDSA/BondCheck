package com.sdidsa.bondcheck.app.app_content.session.content.settings.display;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.Image;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.OverlayOption;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UiScaleOverlay extends PartialSlideOverlay {
    public UiScaleOverlay(Context owner) {
        this(owner, "title", () -> "", (s) -> {});
    }

    public UiScaleOverlay(Context owner, String key, Supplier<String> get,
                          Consumer<String> set, OverlayOption...options) {
        super(owner, -2);

        list.setPadding(20);
        list.setSpacing(20);
        list.setAlignment(Alignment.CENTER);

        HBox top = new HBox(owner);
        top.setAlignment(Alignment.CENTER);

        ColoredLabel title = new ColoredLabel(owner, Style.TEXT_NORM, "set_" + key)
                .setFont(new Font(20, FontWeight.MEDIUM));

        ColoredButton save = new ColoredButton(owner, Style.ACCENT,
                Style.WHITE, "save")
                .setFont(new Font(18, FontWeight.MEDIUM));
        save.setWidth(120);
        save.setPadding(10);

        top.addViews(title, SpacerUtils.spacer(owner, Orientation.HORIZONTAL), save);

        UiScaleSlider slider = new UiScaleSlider(owner);

        ColoredLabel value = new ColoredLabel(owner, Style.TEXT_NORM, get.get())
                .setFont(new Font(18));

        StackPane preSlider = new StackPane(owner);
        preSlider.setClipChildren(false);
        preSlider.setClipToPadding(false);
        preSlider.setPadding(10);
        preSlider.addView(slider);

        Image preview = new Image(owner);
        preview.setBackground(Color.BLACK);
        preview.setCornerRadius(15);
        preview.setSize(256);
        preview.setElevation(SizeUtils.dipToPx(10, owner));

        list.addView(top);
        list.addView(preSlider);
        list.addView(value);
        list.addView(preview);

        slider.setOnChanged((ind) -> {
            value.setKey(options[ind].text());

            float scaleFactor = UiScale.forText(options[ind].text()).getScale();
            Platform.runBack(() -> {
                Bitmap previewBitmap = generatePreviewBitmap(scaleFactor, owner);
                Platform.runLater(() -> preview.setImageBitmap(previewBitmap));
            });
        });

        save.setOnClick(() -> set.accept(options[slider.getSelectedIndex()].text()));

        slider.setTickCount(options.length);

        addOnShowing(() -> {
            slider.setSelectedIndex(options.length / 2, true);
            value.setKey(get.get());
            preview.setImageResource(R.drawable.empty);
            String v = get.get();
            for(int i = 0; i < options.length; i++) {
                String os = options[i].text();
                if(os.equalsIgnoreCase(v)) {
                    slider.setSelectedIndex(i);
                    break;
                }
            }
        });
    }

    private Bitmap generatePreviewBitmap(float scaleFactor, Context context) {
        Style style = StyleUtils.getStyle(context).get();
        int size = ContextUtils.getScreenHeight(owner) / 7;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(style.getBackgroundTertiary());

        Paint paint = new Paint();
        paint.setTextSize(18 * scaleFactor);
        paint.setColor(style.getTextNormal());
        paint.setAntiAlias(true);

        canvas.drawText("Sample Button", 60, 80 * scaleFactor, paint);

        paint.setTextSize(14 * scaleFactor);  // Smaller text
        canvas.drawText("Sample Label", 60, 150 * scaleFactor, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(style.getBackgroundSecondary());
        canvas.drawRect(60, 200 * scaleFactor, 280 * scaleFactor, 240 * scaleFactor, paint);

        paint.setTextSize(16 * scaleFactor);
        paint.setColor(style.getTextSecondary());
        canvas.drawText("Preview UI Scale", 70, 225 * scaleFactor, paint);

        return bitmap;
    }
}
