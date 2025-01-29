package com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail;

import android.content.Context;

import com.sdidsa.bondcheck.models.Feeling;

public class FeelingFragment extends DetailFragment {
    public FeelingFragment(Context owner) {
        super(owner, () -> Feeling.listItems(owner), "feeling_search");
    }
}
