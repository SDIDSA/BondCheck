package com.sdidsa.bondcheck.abs.components.controls.text;

import android.content.Context;

import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

import java.time.LocalDateTime;
import java.util.Date;

public class DateLabel extends Label {
    private LocalDateTime date;
    private DateFormat format = DateFormat.FULL_LONG;

    public DateLabel(Context owner) {
        super(owner);
    }

    public void setFormat(DateFormat format) {
        this.format = format;
        applyLocale(ContextUtils.getLocale(owner).get());
    }

    public void setDate(Date date) {
        setDate(DateFormat.convertToLocalDateTime(date));
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
        applyLocale(ContextUtils.getLocale(owner).get());
    }

    @Override
    public void applyLocale(Locale locale) {
        if(date != null && format != null) {
            setText(format.format(locale, date));
        }
    }
}
