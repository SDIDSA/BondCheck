package com.sdidsa.bondcheck.abs.utils;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.GravityInt;
import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.layout.overlay.Overlay;
import com.sdidsa.bondcheck.abs.data.media.Media;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.components.controls.location.GeoCoder;
import com.sdidsa.bondcheck.models.DBLocation;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ContextUtils {
    public static float scale = 1;

    public static String getDisplayableAddress(DBLocation location, String lang) {
        AtomicReference<String> res = new AtomicReference<>();
        Semaphore waiter = new Semaphore(0);
        GeoCoder.getAddress(location.latitude(), location.longitude(),
                new GeoCoder.OnGeocodeResultListener() {
                    @Override
                    public void onGeocodeResult(String address) {
                        res.set(address);
                        waiter.release();
                    }

                    @Override
                    public void onError(Exception error) {
                        ErrorHandler.handle(error, "reverse geocoding location");
                        waiter.release();
                    }
                }, lang);
        waiter.acquireUninterruptibly();
        return res.get();
    }

    @SuppressLint("MissingPermission")
    public static void getLocationAsync(Context owner, Consumer<Location> onLocation) {
        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(owner);

        if (!isGranted(owner, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            onLocation.accept(null);
            return;
        }
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                null)
                .addOnFailureListener(failure ->
                ErrorHandler.handle(failure, "requesting location"))
                .addOnSuccessListener(location -> {
            if(location != null) {
                onLocation.accept(location);
            }else {
                getApproxLocation(owner, onLocation);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private static void getApproxLocation(Context owner, Consumer<Location> onLocation) {
        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(owner);
        client.getCurrentLocation(Priority.PRIORITY_LOW_POWER,
                        null)
                .addOnFailureListener(failure ->
                        ErrorHandler.handle(failure, "requesting location"))
                .addOnSuccessListener(location -> {
                    if(location != null) {
                        onLocation.accept(location);
                    }else {
                        getApproxLocation(owner, onLocation);
                    }
                });
    }

    public static DBLocation getLocation(Context owner) {
        Log.i("context utils", "requesting location...");
        AtomicReference<Location> res = new AtomicReference<>();
        Semaphore waiter = new Semaphore(0);
        Platform.runAfter(waiter::release, 15000);
        getLocationAsync(owner, location -> {
            res.set(location);
            waiter.release();
        });
        waiter.acquireUninterruptibly();

        if(res.get() == null) {
            return null;
        }else {
            return new DBLocation(res.get().getLatitude(), res.get().getLongitude());
        }
    }

    public static String getLang(Context context) {
        return getLocale(context).get().getLang();
    }

    public static View[] getViewChildren(ViewGroup viewGroup) {
        return getViewChildren(viewGroup, 0);
    }

    public static View[] getViewChildren(ViewGroup viewGroup, int from) {
        View[] res = new View[viewGroup.getChildCount()];
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if(viewGroup.getChildAt(i).getVisibility() == View.VISIBLE)
                res[i] = viewGroup.getChildAt(i);
        }
        return res;
    }

    public static int getLocaleDirection(View view) {
        return getLocaleDirection(view.getContext());
    }

    public static int getLocaleDirection(Context context) {
        return getLocale(context).get().getDirection();
    }

    public static boolean isRtl(Context context) {
        return getLocale(context).get().isRtl();
    }

    public static String getAppName(Context context) {
        return context.getResources().getString(R.string.app_name);
    }

    public static void removeOverlay(Context context, Overlay overlay) {
        if(context instanceof App app) {
            app.removeOverlay(overlay);
        }
    }

    public static void addOverlay(Context context, Overlay overlay) {
        if(context instanceof App app) {
            app.addOverlay(overlay);
        }
    }

    public static void requirePermissions(Context context, Runnable onGranted,
                                         String... permissions) {
        if(context instanceof App app) {
            app.requirePermissions(onGranted, permissions);
        } else if(isGranted(context, permissions)) {
            onGranted.run();
        }
    }

    public static void requestPermissionsOr(Context context, Runnable onGranted, String... permissions) {
        if(context instanceof App app) {
            app.requestPermissionsOr(onGranted, permissions);
        } else if(isGranted(context, permissions)) {
            onGranted.run();
        }
    }

    public static void requirePermissionsOr(Context context, Runnable onGranted,
                                          String... permissions) {
        if(context instanceof App app) {
            app.requirePermissionsOr(onGranted, permissions);
        } else if(isGranted(context, permissions)) {
            onGranted.run();
        }
    }

    public static void requestPermissions(Context context, Runnable onGranted,
                                          String... permissions) {
        if(context instanceof App app) {
            app.requestPermissions(onGranted, permissions);
        }
    }

    public static boolean isGranted(Context context, String...permissions) {
        if(context instanceof App app) {
            return app.isGranted(permissions);
        }else {
            for (String permission : permissions) {
                if (!isGranted(context, permission)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isGranted(Context context, String permission) {
        if(context instanceof App app) {
            return app.isGranted(permission);
        }else {
            return context.checkSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void finishAndRemoveTask(Context context) {
        if(context instanceof App app) {
            app.finishAndRemoveTask();
        }
    }

    public static void moveTaskToBack(Context context, boolean nonRoot) {
        if(context instanceof App app) {
            app.moveTaskToBack(nonRoot);
        }
    }

    public static void setToken(Context context, String token) {
        App.setToken(context, token);
    }

    public static void unloadApp(Context owner, Runnable post) {
        if(owner instanceof App app) {
            app.unloadApp(post);
        }
    }

    public static void loadApp(Context owner, Runnable post) {
        if(owner instanceof App app) {
            app.loadApp(post);
        }
    }

    public static void loadPage(Context context, Class<? extends Page> type) {
        loadPage(context, type, null, 1);
    }

    public static void loadPage(Context context, Class<? extends Page> type, Runnable post) {
        loadPage(context, type, post, 1);
    }

    public static void loadPage(Context context, Class<? extends Page> type, int direction) {
        loadPage(context, type, null, direction);
    }

    public static void loadPage(Context context, Class<? extends Page> type,
                                Runnable post, int direction) {
        if(context instanceof App app) {
            app.loadPage(type, post, direction);
        }
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static void pickImage(Context context, Consumer<Media> onRes) {
        if(context instanceof App app) {
            app.pickImage(onRes);
        }
    }

    private static Property<Locale> locale;
    public static Property<Locale> getLocale(Context context) {
        if(context instanceof App app) {
            return app.getLocale();
        }else {
            if(locale == null) {
                locale = new Property<>(readLocale(context));
            }
            return locale;
        }
    }

    private static Property<Style> style;
    public static Property<Style> getStyle(Context context) {
        if(context instanceof App app) {
            return app.getStyle();
        }else {
            if(style == null) {
                style = new Property<>(readStyle(context));
            }
            return style;
        }
    }

    public static void styleChanged(Context context) {
        getStyle(context).set(readStyle(context));
    }

    public static void localeChanged(Context context) {
        getLocale(context).set(readLocale(context));
    }

    private static Style readStyle(Context context) {
        String theme = Store.getTheme();
        String s = theme.equals(Style.THEME_DARK) ? "dark" :
                theme.equals(Style.THEME_LIGHT) ? "light" :
                        isDarkMode(context.getResources().getConfiguration()) ?
                                "dark" : "light";

        return new Style(context, s, s.equalsIgnoreCase("dark"));
    }

    private static Locale readLocale(Context context) {
        return Locale.forName(context, Store.getLanguage());
    }

    public static void setLocale(Context context, Locale locale) {
        getLocale(context).set(locale);
    }

    public static void setStyle(Context context, Style style) {
        getStyle(context).set(style);
    }


    public static boolean isDarkMode(Configuration newConfig) {
        return (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static Insets getSystemInsets(Context context) {
        if(context instanceof App app) {
            return app.getSystemInsets();
        }
        return Insets.of(0,0,0,0);
    }

    public static void showKeyboard(Context context, EditText input) {
        if(context instanceof App app) {
            app.showKeyboard(input);
        }
    }

    public static void hideKeyboard(Context context) {
        if(context instanceof App app) {
            app.hideKeyboard();
        }
    }

    public static void toast(Context context, String content) {
        if(context instanceof App app) {
            app.toast(content);
        } else {
            Toast.makeText(context, getLocale(context).get().get(content),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void toast(Context context, String content, long duration) {
        if(context instanceof App app) {
            app.toast(content, duration);
        } else {
            Toast.makeText(context, content, Toast.LENGTH_LONG).show();
        }
    }

    public static void startForResult(Context context, Intent intent) {
        if(context instanceof App app) {
            app.startForResult(intent);
        }
    }

    public static void applyTheme(Context context) {
        if(context instanceof App app) {
            app.applyTheme();
        }
    }

    public static void postCreate(Context context) {
        if(context instanceof App app) {
            app.postCreate();
        }
    }

    public static void setPaddingUnified(View view, float padding, Context context) {
        setPadding(view, padding, padding, padding, padding, context);
    }

    public static void setPaddingTop(View view, float padding, Context context) {
        setPadding(view, 0, padding, 0, 0, context);
    }

    public static void setPaddingVertical(View view, float padding, Context context) {
        setPadding(view, 0, padding, 0, padding, context);
    }

    public static void setPaddingHorizontalVertical(View view, float horizontal, float vertical, Context context) {
        setPadding(view, horizontal, vertical, horizontal, vertical, context);
    }

    public static int by(Context owner) {
        return dipToPx(64, owner);
    }

    public static void setPadding(View view, float left, float top, float right, float bottom, Context context) {
        int dil = left == -1 ? view.getPaddingLeft() : dipToPx(left, context);
        int dit = top == -1 ? view.getPaddingTop() : dipToPx(top, context);
        int dir = right == -1 ? view.getPaddingRight() : dipToPx(right, context);
        int dib = bottom == -1 ? view.getPaddingBottom() : dipToPx(bottom, context);
        view.setPaddingRelative(dil, dit, dir, dib);
        //view.setPadding(dil, dit, dir, dib);
    }

    public static void spacer(View view, Orientation orientation) {
        try {
            boolean hor = orientation == Orientation.HORIZONTAL;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    hor ? LinearLayout.LayoutParams.WRAP_CONTENT : -1,
                    hor ? -1 : LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            view.setLayoutParams(params);
        }catch (Exception x) {
            ErrorHandler.handle(x, "creating spacer");
        }
    }

    public static void spacer(View view) {
        int width = view.getLayoutParams() != null ? view.getLayoutParams().width :
                LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = view.getLayoutParams() != null ? view.getLayoutParams().height :
                LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.weight = 1;
        view.setLayoutParams(params);
    }

    public static void spacerWidth(View view, float weight) {
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.weight = weight;
        view.setLayoutParams(params);
    }

    public static View spacer(Context context, Orientation orientation) {
        try {
            boolean hor = orientation == Orientation.HORIZONTAL;
            View view = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    hor ? LinearLayout.LayoutParams.WRAP_CONTENT : -1,
                    hor ? -1 : LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            view.setLayoutParams(params);
            return view;
        }catch (Exception x) {
            ErrorHandler.handle(x, "creating spacer");
            return null;
        }
    }

    public static float pxToDip(int input, Context context) {
        return pxToDip((float) input, context);
    }

    public static float pxToDip(float px, Context context) {
        return px / (context.getResources().getDisplayMetrics().density * scale);
    }

    public static float pxToDipNoScale(float px, Context context) {
        return px / (context.getResources().getDisplayMetrics().density);
    }

    public static int dipToPx(int input, Context context) {
        return (int) (dipToPx((float) input, context) + .5);
    }

    public static int dipToPx(float input, Context context) {
        return (int) (input * context.getResources().getDisplayMetrics().density * scale);
    }

    public static void setMarginTop(View view, Context context, float val) {
        setMargin(view, context, 0, val, 0, 0);
    }

    public static void setMarginUnified(View view, Context context, float val) {
        setMargin(view, context, val, val, val, val);
    }

    public static void setMarginRight(View view, Context context, float val) {
        setMargin(view, context, 0, 0, val, 0);
    }

    public static void setMarginLeft(View view, Context context, float val) {
        setMargin(view, context, val, 0, 0, 0);
    }

    public static void setMarginHorizontal(View view, Context context, float val) {
        setMargin(view, context, val, 0, val, 0);
    }

    public static void setMarginTopRight(View view, Context context, float top, float right) {
        setMargin(view, context, 0, top, right, 0);
    }

    public static void setMarginTopLeft(View view, Context context, float top, float left) {
        setMargin(view, context, left, top, 0, 0);
    }

    public static void setMarginBottom(View view, Context context, float val) {
        setMargin(view, context, 0, 0, 0, val);
    }

    public static void setMargin(View view, Context context, float left, float top, float right, float bottom) {
        try {
            ViewGroup.LayoutParams old = view.getLayoutParams();
            if (old instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams marginLayoutParams = duplicateLinearLayoutParams((LinearLayout.LayoutParams) old);
                marginLayoutParams.setMargins(dipToPx(left, context), dipToPx(top, context), dipToPx(right, context), dipToPx(bottom, context));

                marginLayoutParams.setMarginStart(dipToPx(left, context));
                marginLayoutParams.setMarginEnd(dipToPx(right, context));

                view.setLayoutParams(marginLayoutParams);
            } else if(old instanceof FrameLayout.LayoutParams flp){
                FrameLayout.LayoutParams marginLayoutParams = duplicateFrameLayoutParams(flp);
                marginLayoutParams.setMargins(dipToPx(left, context), dipToPx(top, context), dipToPx(right, context), dipToPx(bottom, context));

                marginLayoutParams.setMarginStart(dipToPx(left, context));
                marginLayoutParams.setMarginEnd(dipToPx(right, context));

                view.setLayoutParams(marginLayoutParams);
            } else {
                ViewGroup.MarginLayoutParams marginLayoutParams = duplicateViewGroupParams(old);
                marginLayoutParams.setMargins(dipToPx(left, context), dipToPx(top, context), dipToPx(right, context), dipToPx(bottom, context));

                marginLayoutParams.setMarginStart(dipToPx(left, context));
                marginLayoutParams.setMarginEnd(dipToPx(right, context));

                view.setLayoutParams(marginLayoutParams);
            }
        }catch(Exception x) {
            ErrorHandler.handle(x, "setting margin");
        }
    }

    public static void applyEnabled(View view, boolean enabled) {
        Drawable drawable = view.getBackground();
        if (drawable != null) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(enabled ? 1 : 0f);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            drawable.setColorFilter(filter);
            view.setBackground(drawable);
        }
    }

    @SuppressWarnings("deprecation")
    public static float pxToSp(float px, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= 34) {
            return TypedValue.convertPixelsToDimension(COMPLEX_UNIT_SP, px, metrics) ;
        } else {
            return px / (context.getResources().getDisplayMetrics().scaledDensity);
        }
    }

    @SuppressWarnings("deprecation")
    public static float spToPx(float sp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= 34) {
            return TypedValue.convertDimensionToPixels(COMPLEX_UNIT_SP, sp, metrics);
        } else {
            return sp * context.getResources().getDisplayMetrics().scaledDensity;
        }
    }

    private static LinearLayout.LayoutParams duplicateLinearLayoutParams(LinearLayout.LayoutParams old) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.MarginLayoutParams.WRAP_CONTENT,
                LinearLayout.MarginLayoutParams.WRAP_CONTENT
        );

        if (old != null) {
            params.height = old.height;
            params.width = old.width;

            params.weight = old.weight;
            params.gravity = old.gravity;
            params.bottomMargin = old.bottomMargin;
            params.topMargin = old.topMargin;
            params.rightMargin = old.rightMargin;
            params.leftMargin = old.leftMargin;
        }

        return params;
    }

    private static FrameLayout.LayoutParams duplicateFrameLayoutParams(FrameLayout.LayoutParams old) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LinearLayout.MarginLayoutParams.WRAP_CONTENT,
                LinearLayout.MarginLayoutParams.WRAP_CONTENT
        );

        if (old != null) {
            params.height = old.height;
            params.width = old.width;

            params.gravity = old.gravity;
            params.bottomMargin = old.bottomMargin;
            params.topMargin = old.topMargin;
            params.rightMargin = old.rightMargin;
            params.leftMargin = old.leftMargin;
        }

        return params;
    }

    public static void alignInFrame(View view, Alignment alignment) {
        alignInFrame(view, alignment.getGravity());
    }

    private static void alignInFrame(View view, @GravityInt int gravity) {
        try {
            ViewGroup.LayoutParams old = view.getLayoutParams();
            StackPane.LayoutParams n = new StackPane.LayoutParams(
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT
            );

            if(old != null) {
                n.width = old.width;
                n.height = old.height;
                if(old instanceof ViewGroup.MarginLayoutParams mold) {
                    n.setMargins(mold.leftMargin, mold.topMargin, mold.rightMargin, mold.bottomMargin);
                }
            }
            n.gravity = gravity;
            view.setLayoutParams(n);
        }catch(Exception x) {
            ErrorHandler.handle(x, "aligning child in frame");
        }
    }

    private static ViewGroup.MarginLayoutParams duplicateViewGroupParams(ViewGroup.LayoutParams old) {
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
        );

        if (old != null) {
            params.height = old.height;
            params.width = old.width;

            if (old instanceof ViewGroup.MarginLayoutParams marginedOld) {
                params.bottomMargin = marginedOld.bottomMargin;
                params.topMargin = marginedOld.topMargin;
                params.rightMargin = marginedOld.rightMargin;
                params.leftMargin = marginedOld.leftMargin;
            }
        }

        return params;
    }

    public static int dipToPx(Double aDouble, Context owner) {
        return dipToPx(aDouble.floatValue(), owner);
    }
}
