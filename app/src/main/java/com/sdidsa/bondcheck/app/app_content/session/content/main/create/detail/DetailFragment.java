package com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.input.MinimalInputField;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.scroll.Recycler;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.models.PostDetail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DetailFragment extends Fragment {
    private final List<PostDetail> items;
    private final MinimalInputField search;

    private final Recycler<PostDetail, DetailItemDisplay> sv;

    public DetailFragment(Context owner) {
        this(owner, ArrayList::new, "Search...");
    }

    public DetailFragment(Context owner, Supplier<List<PostDetail>> itemSupplier, String hint) {
        super(owner);
        setSpacing(10);

        search = new MinimalInputField(owner,
                hint);
        search.setBackFill(Style.BACK_PRI);

        search.setFont(new Font(18));

        search.addPostInput(
                new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.search, 44)
                        .setImagePadding(10));

        items = itemSupplier.get();

        sv = new Recycler<>(owner, DetailItemDisplay.class);
        sv.setItems(items);
        sv.setClipToOutline(true);

        search.valueProperty().addListener((ov, nv) -> {
            if (ov.equals(nv)) return;

            if (!nv.isEmpty()) {
                List<PostDetail> matches =
                        items.stream()
                                .filter(i -> i.match(nv))
                                .sorted(Comparator.comparing(PostDetail::description))
                                .collect(Collectors.toList());
                sv.setItems(matches);
            } else if (!ov.isEmpty()) {
                sv.setItems(items);
            }
        });

        addView(search);
        addView(sv);
    }

    public void reset() {
        sv.scrollTo(0,0);
        search.setValue("");
    }
}
