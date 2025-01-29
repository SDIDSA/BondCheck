package com.sdidsa.bondcheck.app.app_content.session.content.main.create;

import android.content.Context;

import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.components.controls.input.MinimalInputField;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.fragment.FragmentPane;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.CreateMain;

public class Create extends FragmentPane {
    public Create(Context owner) {
        super(owner, CreateFragment.class);

        nextInto(CreateMain.class);
    }

    public ParallelAnimation getShow() {
        return Fragment.getInstance(owner, CreateMain.class).getShow();
    }

    public MinimalInputField getHead() {
        return Fragment.getInstance(owner, CreateMain.class).getHead();
    }

    public boolean back() {
        if(getLoaded() instanceof CreateMain) {
            return false;
        } else {
            previousInto(CreateMain.class);
            return true;
        }
    }
}
