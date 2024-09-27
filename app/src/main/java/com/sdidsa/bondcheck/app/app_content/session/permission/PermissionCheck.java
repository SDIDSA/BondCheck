package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredVBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.BondCheck;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PermissionCheck extends Page {
    private final Button ready;
    private final ColoredLabel header;

    private final String notReadyText = "Before you can use BondCheck, you need to grant it " +
            "the following permissions";
    private final String readyText = "Thank you for giving BondCheck all the permissions it requires";
    public PermissionCheck(Context owner) {
        super(owner);

        VBox root = new VBox(owner);
        root.setPadding(20);
        root.setSpacing(30);

        header = new ColoredLabel(owner,
                Style.TEXT_SEC, notReadyText
        );

        header.centerText();
        header.setLineSpacing(5);
        header.setFont(new Font(20));

        ready = new ColoredButton(owner, Style.ACCENT, s -> Color.WHITE, "You're ready");
        ready.setFont(new Font(20, FontWeight.MEDIUM));
        ready.setOnClick(() -> BondCheck.loadSession(owner));
        ready.setEnabled(false);

        root.addView(header);

        VBox items = new VBox(owner);
        items.setSpacing(20);

        permView(items, "Display notifications",
                () -> requestNotificationPermission(owner),
                () -> hasNotificationPermission(owner),
                () -> {
                    WhyPermissionOverlay ov = new WhyNotifications(owner);
                    ov.show();
                });

        permView(items, "Microphone Access",
                () -> requestMicrophonePermission(owner),
                () -> hasMicrophonePermission(owner),
                PermissionCheck::isSkipMicrophone,
                PermissionCheck::setSkipMicrophone,
                () -> {
                    WhyPermissionOverlay ov = new WhyMicrophone(owner);
                    ov.show();
                });

        permView(items, "All time Location",
                () -> requestLocationPermission(owner),
                () -> hasLocationPermission(owner),
                PermissionCheck::isSkipLocation,
                PermissionCheck::setSkipLocation,
                () -> {
                    WhyPermissionOverlay ov = new WhyLocation(owner);
                    ov.show();
                });

        permView(items, "Usage Access",
                () -> requestUsageAccess(owner),
                () -> hasUsageAccessPermission(owner),
                PermissionCheck::isSkipUsageStats,
                PermissionCheck::setSkipUsageStats,
                () -> {
                    WhyPermissionOverlay ov = new WhyUsage(owner);
                    ov.show();
                });

        permView(items, "Ignore Battery Optimizations",
                () -> requestBatteryPermission(owner),
                () -> hasBatteryPermission(owner),
                () -> {
                    WhyPermissionOverlay ov = new WhyBattery(owner);
                    ov.show();
                });

        permView(items, "Draw Over Other Apps",
                () -> requestOverlayPermission(owner),
                () -> hasOverlayPermission(owner),
                () -> {
                    WhyPermissionOverlay ov = new WhyOverlay(owner);
                    ov.show();
                });

        ScrollView preItems = new ScrollView(owner);
        preItems.setVerticalScrollBarEnabled(false);
        preItems.setClipToOutline(true);
        preItems.addView(items);

        ContextUtils.spacer(preItems, Orientation.VERTICAL);

        root.addView(preItems);
        root.addView(ready);
        addView(root);

    }

    private void permView(VBox root, String name, Runnable request,
                          Supplier<Boolean> isGranted,
                          Runnable onWhy) {
        permView(root, name, request, isGranted, () -> false, null, onWhy);
    }

    private void permView(VBox root, String name, Runnable request,
                          Supplier<Boolean> isGranted,
                          Supplier<Boolean> isSkipped,
                          Consumer<Boolean> setSkipped,
                          Runnable onWhy) {
        if(isGranted.get()) {
           return;
        }

        VBox res = new ColoredVBox(owner, Style.BACK_SEC);
        HBox top = new HBox(owner);

        ColoredLabel header = new ColoredLabel(owner,
                Style.TEXT_NORM, name
        ).setFont(new Font(18));

        ColoredLabel label = new ColoredLabel(owner,
                Style.TEXT_ERR, "Not granted"
        ).setFont(new Font(18));

        HBox grant = new HBox(owner);
        ColoredButton grantNow = button("Grant now", R.drawable.arrow_right);
        grantNow.setTextFill(Style.TEXT_NORM);
        ColoredButton why = button("Why", R.drawable.why);

        res.setPadding(15);
        res.setSpacing(20);
        res.setCornerRadius(15);

        top.addView(header);
        top.addView(ContextUtils.spacer(owner, Orientation.HORIZONTAL));

        top.addView(label);

        grant.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        if(onWhy != null) {
            why.setOnClick(onWhy);
        }
        ContextUtils.setMarginRight(why, owner, 15);

        grant.addViews(why, grantNow);

        grantNow.setOnClick(request);

        Runnable init = () -> {
            res.removeAllViews();
            res.addView(top);
            res.addView(grant);

            label.setFill(Style.TEXT_ERR);
            label.setKey("Not granted");

            Platform.waitWhile(() -> !isGranted.get() && !isSkipped.get(), () -> {
                if(isGranted.get()) {
                    label.setFill(Style.TEXT_POS);
                    label.setKey("Granted");
                } else if(isSkipped.get()) {
                    label.setFill(Style.TEXT_SEC);
                    label.setKey("Skipped");
                }
                res.removeView(grant);

                Intent i = new Intent(owner, BondCheck.class);
                i.setAction(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                owner.startActivity(i);

                if(!shouldShow(owner)) {
                    this.header.setKey(readyText);
                    ready.setEnabled(true);
                }
            });
        };

        init.run();

        res.setOnClickListener(e -> {
            if(isSkipped.get() && setSkipped != null) {
                setSkipped.accept(false);
                Platform.waitWhile(isSkipped, init);
                if(shouldShow(owner)) {
                    this.header.setKey(notReadyText);
                    ready.setEnabled(false);
                }
            }
        });

        root.addView(res);
    }

    private ColoredButton button(String text, @DrawableRes int iconRes) {
        ColoredButton res = new ColoredButton(owner, Style.BACK_PRI,
                Style.TEXT_SEC,
                text);
        res.extendLabel();
        ColoredIcon icon = new ColoredIcon(owner, Style.TEXT_SEC, iconRes, 20);
        res.addPostLabel(icon);
        res.setElevation(0);
        res.setFont(new Font(18));
        ContextUtils.spacer(res);
        return res;
    }

    public static boolean shouldShow(Context owner) {
        HashSet<Boolean> perms = new HashSet<>();
        perms.add(hasUsageAccessPermission(owner) || isSkipUsageStats());
        perms.add(hasMicrophonePermission(owner) || isSkipMicrophone());
        perms.add(hasLocationPermission(owner) || isSkipLocation());
        perms.add(hasBatteryPermission(owner));
        perms.add(hasNotificationPermission(owner));

        return !perms
                .stream().filter(e -> !e)
                .collect(Collectors.toSet())
                .isEmpty();
    }

    public static void requestNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33
            ContextUtils.requestPermissions(context, null,
                    android.Manifest.permission.POST_NOTIFICATIONS);
        }else {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
        }
    }

    @SuppressLint("BatteryLife")
    public static void requestBatteryPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    public static void requestMicrophonePermission(Context context) {
        ContextUtils.requestPermissions(context,null,
                android.Manifest.permission.RECORD_AUDIO);
    }

    public static void requestLocationPermission(Context context) {
        ContextUtils.requirePermissionsOr(context, () ->
                        ContextUtils.requestPermissions(context, null,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static void requestUsageAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }

    public static void requestOverlayPermission(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    //PERMISSION CHECK
    public static boolean hasNotificationPermission(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static boolean hasBatteryPermission(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return false;
    }

    public static boolean hasMicrophonePermission(Context context) {
        return ContextUtils.isGranted(context, android.Manifest.permission.RECORD_AUDIO);
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextUtils.isGranted(context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    public static boolean hasUsageAccessPermission(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String packageName = context.getPackageName();
        int mode = appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, packageName);
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static boolean hasOverlayPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    //SKIP METHODS
    public static boolean isSkipLocation() {
        return Store.isSkipLocationPermission();
    }

    public static boolean isSkipMicrophone() {
        return Store.isSkipMicrophonePermission();
    }

    public static boolean isSkipUsageStats() {
        return Store.isSkipUsageStatsPermission();
    }

    public static void setSkipLocation(boolean value) {
        Store.setSkipLocationPermission(value, null);
    }

    public static void setSkipMicrophone(boolean value) {
        Store.setSkipMicrophonePermission(value, null);
    }

    public static void setSkipUsageStats(boolean value) {
        Store.setSkipUsageStatsPermission(value, null);
    }

    @Override
    public Animation setup(int direction) {
        if(setup == null) {
            setup = new ParallelAnimation(400)
                    .addAnimation(direction > 0 ? Animation.fadeInScaleUp(this):
                            Animation.fadeInScaleDown(this))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        setAlpha(0);
        return setup;
    }

    @Override
    public Animation destroy(int direction) {
        if(destroy == null) {
            destroy = new ParallelAnimation(400)
                    .addAnimation(direction > 0 ? Animation.fadeOutScaleUp(this): Animation.fadeOutScaleDown(this))
                    .setInterpolator(Interpolator.ANTICIPATE_OVERSHOOT);
        }
        return destroy;
    }

    @Override
    public boolean onBack() {
        ContextUtils.moveTaskToBack(owner, true);
        return true;
    }

    @Override
    public void applyInsets(Insets insets) {
        setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }
}
