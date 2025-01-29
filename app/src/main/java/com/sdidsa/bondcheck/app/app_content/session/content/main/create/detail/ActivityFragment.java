package com.sdidsa.bondcheck.app.app_content.session.content.main.create.detail;

import android.content.Context;

import com.sdidsa.bondcheck.models.Activity;

public class ActivityFragment extends DetailFragment {
    public ActivityFragment(Context owner) {
        super(owner, () -> Activity.listItems(owner), "activity_search");
    }
}
