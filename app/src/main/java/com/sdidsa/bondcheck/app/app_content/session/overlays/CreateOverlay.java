package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.overlay.RelativeOverlay;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.Create;


public class CreateOverlay extends RelativeOverlay {

    private final Create create;

    public CreateOverlay(Context owner) {
        super(owner);


        create = new Create(owner);
        root.addAligned(create, Alignment.TOP_RIGHT);

        addOnShown(() -> {
            create.getHead().getInput().requestFocus();
            ContextUtils.showKeyboard(owner, create.getHead().getInput());
        });

        addToShow(create.getShow());
        addToHide(create.getShow().reverse());

        addOnHidden(() -> ContextUtils.hideKeyboard(owner));
    }

    @Override
    public void back() {
        if(!create.back()) {
            super.back();
        }
    }
}
