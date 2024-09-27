package com.sdidsa.bondcheck.app.app_content.session.content.main;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.HomePage;
import com.sdidsa.bondcheck.app.app_content.session.content.history.History;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondStatus;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.Gender;

public class Main extends HomePage {
    private final NetImage img;
    private final BondStatus bondStatus;

    public Main(Context owner) {
        super(owner, "home");

        ContextUtils.setPadding(content, 15, 0, 15, 15, owner);
        content.setSpacing(0);

        bondStatus = new BondStatus(owner);

        img = new NetImage(owner);
        img.setSize(48);
        img.setCornerRadius(48);
        img.setPadding(5);

        top.addView(img);

        img.setOnClick(() -> {
            Settings settings = Fragment.getInstance(owner, Settings.class);
            assert settings != null;
            settings.getAccountGroup().showUserProfile();
        });

        img.startLoading();
        SessionService.getUser(owner, Store.getUserId(), resp -> {
            resp.avatar().addListener((ov, nv) ->
            {
                if(nv != null) {
                    ImageProxy.getImageThumb(owner, nv, ContextUtils.dipToPx(48, owner),
                            img::setImageBitmap);
                }else {
                    Gender g = resp.genderValue();
                    if(g == Gender.Female) {
                        img.setImageResource(R.drawable.avatar_female);
                    }else {
                        img.setImageResource(R.drawable.avatar_male);
                    }
                }
            });

            resp.gender().addListener((ov, nv) -> {
                if(resp.getAvatar() == null) {
                    if(resp.genderValue() == Gender.Female) {
                        img.setImageResource(R.drawable.avatar_female);
                    }else {
                        img.setImageResource(R.drawable.avatar_male);
                    }
                }

            });
        });

        Fragment.getInstance(owner, History.class).init(bondStatus);

        bondStatus.fetch();

        content.addView(bondStatus);
    }

    public BondStatus getBondStatus() {
        return bondStatus;
    }
}
