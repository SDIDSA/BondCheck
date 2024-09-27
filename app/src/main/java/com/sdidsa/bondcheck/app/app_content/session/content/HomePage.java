package com.sdidsa.bondcheck.app.app_content.session.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.controls.text.transformationMethods.Capitalize;
import com.sdidsa.bondcheck.abs.components.layout.ScrollView;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public abstract class HomePage extends Fragment {
    protected final VBox content;
    protected final HBox top;
    protected final VBox root;

    protected final ScrollView scrollable;

    @SuppressLint("ClickableViewAccessibility")
    public HomePage(Context owner, String text) {
        super(owner);

        root = new VBox(owner);
        root.setGravity(Gravity.TOP | Gravity.CENTER);
        root.setClipChildren(false);

        top = new ColoredHBox(owner, Style.BACK_PRI);
        top.setGravity(Gravity.CENTER);
        top.setMinimumHeight(ContextUtils.dipToPx(66, owner));
        top.setZ(2000);

        ColoredLabel title = new ColoredLabel(owner, Style.TEXT_NORM, text);
        title.setFont(new Font(24, FontWeight.MEDIUM));
        title.setTransformationMethod(new Capitalize());

        top.addView(title);
        top.addView(ContextUtils.spacer(owner, Orientation.HORIZONTAL));

        content = new VBox(owner);
        content.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        content.setPadding(20);
        content.setSpacing(20);

        scrollable = new ScrollView(owner);
        scrollable.addView(content);

        ContextUtils.spacer(root, Orientation.VERTICAL);

        root.addView(top);
        root.addView(scrollable);

        applyInsets(ContextUtils.getSystemInsets(owner));
        addView(root);
    }

    public void applyInsets(Insets insets) {
        if(insets == null) return;
        int pad = ContextUtils.dipToPx(20, owner);
        top.setPadding(pad, insets.top + pad, pad, pad);
    }
}