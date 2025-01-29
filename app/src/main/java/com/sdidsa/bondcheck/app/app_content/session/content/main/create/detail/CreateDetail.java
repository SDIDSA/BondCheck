package com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.layout.fragment.FragmentPane;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.CreateFragment;

public class CreateDetail extends CreateFragment {

    private final FragmentPane content;

    public CreateDetail(Context owner) {
        super(owner);
        setPadding(15);
        setSpacing(15);

        CreateDetailTop top = new CreateDetailTop(owner, this);

        content = new FragmentPane(owner, DetailFragment.class);

        content.nextInto(FeelingFragment.class);

        addView(top);
        addView(content);
    }

    public FragmentPane getContent() {
        return content;
    }
}
