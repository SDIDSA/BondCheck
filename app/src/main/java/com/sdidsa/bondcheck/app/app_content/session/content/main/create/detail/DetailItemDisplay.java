package com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail;

import android.content.Context;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;

import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.scroll.RecyclerItemView;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.CreateMain;
import com.sdidsa.bondcheck.models.PostDetail;

public class DetailItemDisplay extends RecyclerItemView<PostDetail> {
    private final AppCompatTextView img;
    private final ColoredLabel description;
    private PostDetail item;

    public DetailItemDisplay(Context owner) {
        super(owner);
        HBox root = new HBox(owner);
        root.setAlignment(Alignment.CENTER_LEFT);

        int sizeDp = 32;
        int sizePx = SizeUtils.dipToPx(sizeDp, owner);

        img = new ColoredLabel(owner, Style.TEXT_NORM, "");
        img.setEmojiCompatEnabled(true);
        img.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx);

        description = new ColoredLabel(owner, Style.TEXT_SEC, "")
                .setFont(new Font(20));
        MarginUtils.setMarginLeft(description, owner, 15);

        root.setOnClickListener(e -> Fragment.getInstance(owner, CreateMain.class).onDetail(item));

        root.addView(img);
        root.addView(description);
        addView(root);
    }

    @Override
    public void load(PostDetail item) {
        this.item = item;
        img.setText(item.emoji());
        description.setKey(item.description());
    }
}
