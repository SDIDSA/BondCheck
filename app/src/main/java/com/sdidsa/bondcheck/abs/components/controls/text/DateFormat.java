package com.sdidsa.bondcheck.abs.components.controls.text;

import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public interface DateFormat {

    String format(Locale locale, LocalDateTime date);

    DateFormat FULL_LONG = (locale, date) ->
            combine(
                    date.getDayOfMonth(),
                    locale.get("month_" + date.getMonthValue()),
                    date.getYear(),
                    locale.get("time_at"),
                    pad(date.getHour()) + ":" + pad(date.getMinute())
            );

    DateFormat FULL_SHORT = (locale, date) ->
            combine(
                    date.getDayOfMonth(),
                    locale.get("month_" + date.getMonthValue() + "_short"),
                    date.getYear(),
                    locale.get("time_at"),
                    pad(date.getHour()) + ":" + pad(date.getMinute())
            );

    DateFormat RELATIVE = (locale, date) -> {
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(date, now);
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return locale.get("time_just_now");
        }else if(minutes == 1) {
            return locale.get("time_1_minute_ago");
        } else if (minutes < 60) {
            return locale.get("time_x_minutes_ago" + (minutes <= 10 ? "_sub_10" : ""))
                    .replace("{$0}", Long.toString(minutes));
        }else if(hours == 1) {
            return locale.get("time_1_hour_ago");
        } else if (hours < 12) {
            return locale.get("time_x_hours_ago" + (hours <= 10 ? "_sub_10" : ""))
                    .replace("{$0}", Long.toString(hours));
        } else if(sameDay(date, now)) {
            return locale.get("time_today")
                    .replace("{$0}",
                            pad(date.getHour()) + ":" + pad(date.getMinute()));
        }else if (days < 2) {
            return locale.get("time_yesterday");
        } else if (days < 7) {
            return locale.get("time_x_days_ago")
                    .replace("{$0}", Long.toString(days));
        } else if (days < 365) {
            return combine(date.getDayOfMonth(),
                    locale.get("month_" + date.getMonthValue()));
        } else {
            return combine(
                    date.getDayOfMonth(),
                    locale.get("month_" + date.getMonthValue() + "_short"),
                    date.getYear());
        }
    };

    private static String pad(int val) {
        return (val < 10 ? "0":"") + val;
    }

    private static String combine(Object...objects) {
        StringBuilder builder = new StringBuilder();
        for(Object object : objects) {
            builder.append(object).append(" ");
        }
        return builder.toString().trim();
    }

    static Date parseDbString(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            ErrorHandler.handle(e, "parsing date string");
            return null;
        }
    }

    static LocalDateTime convertToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    static Duration between(Date date1, Date date2) {
        return Duration.between(date1.toInstant(), date2.toInstant());
    }

    private static boolean sameDay(LocalDateTime date1, LocalDateTime date2) {
        return date1.getDayOfYear() == date2.getDayOfYear() &&
                date1.getYear() == date2.getYear();
    }
}
