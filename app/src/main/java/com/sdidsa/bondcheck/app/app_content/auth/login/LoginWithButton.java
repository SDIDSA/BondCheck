package com.sdidsa.bondcheck.app.app_content.auth.login;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.Image;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class LoginWithButton extends ColoredButton {
    public LoginWithButton(Context owner) {
        this(owner, "google", R.drawable.google);
    }

    public LoginWithButton(Context owner, String service, @DrawableRes int iconRes) {
        super(owner, Style.BACK_SEC, Style.TEXT_SEC, "login_with_" + service);
        setFont(new Font(17, FontWeight.MEDIUM));
        Image icon = new ColorIcon(owner,iconRes, 26);
        ContextUtils.setMarginRight(icon, owner, 15);
        addPreLabel(icon);
        setElevation(0);

        setContentDescription("Login with " + service);
    }
}
