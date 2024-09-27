package com.sdidsa.bondcheck.abs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.base.ValueAnimation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateYAnimation;
import com.sdidsa.bondcheck.abs.animation.view.scale.ScaleXYAnimation;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.components.controls.audio.AudioProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.location.AddressProxy;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredVBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.Overlay;
import com.sdidsa.bondcheck.abs.components.layout.overlay.media.MediaPickerOverlay;
import com.sdidsa.bondcheck.abs.data.media.Media;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.OnPermission;
import com.sdidsa.bondcheck.abs.utils.Permissions;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.TransparentActivity;
import com.sdidsa.bondcheck.abs.components.controls.audio.AudioRecorder;
import com.sdidsa.bondcheck.http.ApiService;
import com.sdidsa.bondcheck.http.AuthInterceptor;
import com.sdidsa.bondcheck.http.Socket;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.ModelAdapter;
import com.sdidsa.bondcheck.models.requests.SaveItemRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends AppCompatActivity {
    private static final int toastDur = 2000;
    private static WeakReference<AuthInterceptor> tokener;
    private static ApiService apiService;
    private final ArrayList<Overlay> loadedOverlay = new ArrayList<>();
    private final ArrayList<View> oldToasts = new ArrayList<>();
    private final ArrayList<ParallelAnimation> toasting = new ArrayList<>();
    private final HashMap<Integer, OnPermission> onPermission = new HashMap<>();
    public Style dark, light;
    public Locale ar_ar;
    Animation running = null;
    private StackPane root;
    private Page loaded;
    private Property<Style> style;
    private Property<Locale> locale;
    private Insets systemInsets;
    private MediaPickerOverlay mediaPicker;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Animation theming;

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static ApiService api(Context context) {
        if(apiService == null || tokener.get().getOwner() != context) {
            tokener = new WeakReference<>(new AuthInterceptor(context, Store.getJwtToken()));

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(tokener.get()).build();

            TypeAdapter<JsonObject> delegate = new Gson().getAdapter(JsonObject.class);

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(UserResponse.class,
                    new ModelAdapter<>(delegate, UserResponse.class))
                    .create();

            Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiService.BASE_URL).client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)).build();
            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }

    public static boolean requestMic(Context context, String requester, int duration) {
        AudioRecorder recorder = new AudioRecorder();
        if(recorder.startRecording(context)) {
            Platform.runAfter(() -> recorder.stopRecording(bytes ->
                    Platform.runBack(() -> {
                        DBLocation location = ContextUtils.getLocation(context);
                        SaveItemRequest save = new SaveItemRequest(requester, bytes, location);
                        Call<GenericResponse> call = save.saveRecord(context);

                        Service.enqueue(call, resp -> {
                            if (resp.isSuccessful()) {
                                Log.i("record", "saved");
                            } else {
                                Log.e("record", "failed to save");
                            }
                        });
                    })), duration * 1000L);
            return true;
        }
        return false;
    }

    public static void requestLocation(Context context, Socket socket, String requester) {
        Platform.runBack(() -> {
            DBLocation location = ContextUtils.getLocation(context);
            if(location != null) {
                Call<GenericResponse> call = api(context).saveLocation(
                        new SaveItemRequest(requester, location));

                Service.enqueue(call, resp -> {
                    if (resp.isSuccessful()) {
                        Log.i("location", "saved");
                    } else {
                        Log.e("location", "failed to save");
                    }
                });
            } else {
                socket.emit("location_off",
                        "requester", requester);
            }
        });
    }

    public static void requestCapture(Context context, String requester) {
        context.sendBroadcast(broadcast(Action.KILL_ACTIVITY));

        Intent intent = new Intent(context, TransparentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("requester", requester);
        context.startActivity(intent);
    }

    public static Intent broadcast(Action action) {
        Intent intent = new Intent(Action.INTENT_FILTER);
        intent.putExtra("broadcast_action", action.name());
        return intent;
    }

    public static Intent broadcastSocketEvent(String event, JSONObject data) {
        Intent intent = new Intent(Action.INTENT_FILTER);
        intent.putExtra("broadcast_action", Action.SOCKET_EVENT.name());
        intent.putExtra("event", event);
        intent.putExtra("data", data == null ? null : data.toString());
        return intent;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Store.init(this);
        ImageProxy.init(this);
        AudioProxy.init(this);
        AddressProxy.init(this);


        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                Log.i("got result for ", data.toString());
            }
        });

        root = new StackPane(this);

        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            WindowInsets wi = root.getRootWindowInsets();

            Insets win = wi == null ? Insets.NONE : Insets.of(wi.getSystemWindowInsetLeft(),
                    wi.getSystemWindowInsetTop(),
                    wi.getSystemWindowInsetRight(),
                    wi.getSystemWindowInsetBottom());

            if (!win.equals(systemInsets)) {
                this.systemInsets = win;

                if (loaded != null) {
                    loaded.applyInsets(systemInsets);
                }

                if (!loadedOverlay.isEmpty()) {
                    for (Overlay overlay : loadedOverlay) {
                        overlay.applySystemInsets(systemInsets);
                    }
                }
            }
        });

        Platform.runBack(() -> {
            Page.clearCache();



            dark = new Style(this, "dark", true);
            light = new Style(this, "light", false);

            style = new Property<>();
            applyTheme();

            style.addListener((ov, nv) -> Platform.runLater(() -> applyStyle(nv)));

            root.setClipChildren(false);
            Platform.runLater(() -> setContentView(root));

            Platform.runLater(() ->
                    WindowCompat.
                            setDecorFitsSystemWindows(this.getWindow(), false));

            new Locale(this, "fr_FR");
            new Locale(this, "en_US");
            ar_ar = new Locale(this, "ar_AR", true);

            Font.init(this);

            locale = new Property<>(Locale.forName(this, Store.getLanguage()));
            locale.addListener((ov, nv) ->
                    Platform.runLater(() -> {
                        if (nv != null && nv.isRtl()) {
                            root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        } else {
                            root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                        }
            }));
        }, this::postCreate);
    }

    public void postCreate() {
    }

    public void startForResult(Intent intent) {
        activityResultLauncher.launch(intent);
    }

    public static void setToken(Context context, String token) {
        if(apiService == null || tokener.get().getOwner() != context) {
            api(context);
        }
        tokener.get().setToken(token);
    }

    public void requirePermissions(Runnable onGranted, String... permissions) {
        if (isGranted(permissions)) {
            onGranted.run();
        } else {
            requestPermissions(onGranted, permissions);
        }
    }

    public void requirePermissionsOr(Runnable onGranted, String... permissions) {
        if (isGrantedOr(permissions)) {
            onGranted.run();
        } else {
            requestPermissionsOr(onGranted, permissions);
        }
    }

    public void requestPermissions(Runnable onGranted, String... permissions) {
        int code = Permissions.permissionRequestCode();
        if (onGranted != null) onPermission.put(code, new OnPermission(onGranted));
        requestPermissions(permissions, code);
    }

    public void requestPermissionsOr(Runnable onGranted, String... permissions) {
        int code = Permissions.permissionRequestCode();
        if (onGranted != null) onPermission.put(code, new OnPermission(onGranted, true));
        requestPermissions(permissions, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        OnPermission handler = onPermission.get(requestCode);
        if (handler != null &&
                (handler.isOr() ? isGrantedOr(permissions) : isGranted(permissions))) {
            handler.getOnPermission().run();
            onPermission.remove(requestCode);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean isGranted(String[] permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean isGrantedOr(String[] permissions) {
        for (String permission : permissions) {
            if (isGranted(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGranted(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void pickImage(Consumer<Media> onRes) {
        if (mediaPicker == null) {
            mediaPicker = new MediaPickerOverlay(this);
        }
        mediaPicker.setOnMedia(onRes);
        mediaPicker.show();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void loadPage(Class<? extends Page> pageType) {
        loadPage(pageType, null, 1);
    }

    public void loadPage(Class<? extends Page> pageType, Runnable post, int direction) {
        hideKeyboard();

        if(pageType.isInstance(loaded)) {
            if(post != null) post.run();
            return;
        }

        Platform.runBack(() -> {
            if (running != null && running.isRunning()) {
                Platform.waitWhile(running::isRunning);
            }

            if (loaded != null && pageType.isInstance(loaded) && Page.hasInstance(pageType)) {
                Platform.runLater(() -> loaded.setup(direction));
                return;
            }

            AtomicReference<Page> page = new AtomicReference<>();
            Page old = loaded;
            loaded = null;
            if (old != null) {
                running = old.destroy(direction);
                running.setOnFinished(() -> root.removeView(old));
            }

            Semaphore waiter = new Semaphore(0);
            Platform.runBack(() -> {
                if (old != null) Platform.sleepReal(10);
                waiter.acquireUninterruptibly();
                Platform.runLater(() -> {
                    if (running != null) running.start();
                    page.get().setAlpha(1);
                    page.get().setup(direction)
                            .setOnFinished(() -> {
                                if (post != null) post.run();
                            }).start();
                    try {
                        ((ViewGroup) page.get().getParent()).removeView(page.get());
                    } catch (Exception x) {
                        //ignore
                    }
                    root.addView(page.get(), 0);
                });
            });

            page.set(Page.getInstance(this, pageType));
            waiter.release();

            if (page.get() == null) {
                ErrorHandler.handle(new Exception("Page instance is null"),
                        "Failed to load page: " + pageType.getSimpleName());
                return;
            }

            Platform.runLater(() -> {
                loaded = page.get();
                loaded.applyInsets(this.getSystemInsets());
                loaded.setAlpha(1);
                loaded.setScaleX(1);
                loaded.setScaleY(1);
                loaded.setTranslationY(0);
                loaded.setTranslationX(0);
            });
        });
    }

    public void addOverlay(Overlay overlay) {
        root.addView(overlay);
        overlay.applySystemInsets(systemInsets);
        loadedOverlay.add(0, overlay);
    }

    public void removeOverlay(Overlay overlay) {
        root.removeView(overlay);
        loadedOverlay.remove(overlay);
    }

    public void unloadApp(Runnable post) {
        Animation.fadeOutScaleDown(root)
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    root.removeAllViews();
                    loaded = null;
                    loadedOverlay.clear();
                    Page.clearCache();
                    Fragment.clearCache();
                    if(post != null) {
                        post.run();
                    }
                }).start();
    }

    public void loadApp(Runnable post) {
        Animation.fadeInScaleUp(root)
                .setInterpolator(Interpolator.OVERSHOOT)
                .setOnFinished(() -> {
                    if(post != null) {
                        post.run();
                    }
                })
                .start();
    }

    public void toast(long duration, View... views) {
        Platform.runLater(() -> toastPrivate(duration, views));
    }

    private void toastPrivate(long duration, View... views) {
        VBox toast = new ColoredVBox(this, Style.BACK_SEC);
        toast.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        StackPane.LayoutParams params = new StackPane.LayoutParams(StackPane.LayoutParams.MATCH_PARENT, StackPane.LayoutParams.WRAP_CONTENT);

        int margins = ContextUtils.dipToPx(30, this);
        params.setMargins(margins, margins, margins, (getScreenHeight() / 10) + systemInsets.bottom);
        params.gravity = Gravity.BOTTOM;

        toast.setLayoutParams(params);

        toast.setCornerRadius(12);
        toast.setPadding(15);
        toast.setSpacing(15);

        toast.setElevation(ContextUtils.dipToPx(15, this));

        toast.setAlpha(0);
        toast.setTranslationY(ContextUtils.dipToPx(100, this));
        toast.setScaleX(.7f);
        toast.setScaleY(.7f);

        for (View view : views) {
            toast.addView(view);
        }

        root.addView(toast);

        ParallelAnimation anim = new ParallelAnimation(300).addAnimation(new AlphaAnimation(toast, 1f)).addAnimation(new TranslateYAnimation(toast, 0)).addAnimation(new ScaleXYAnimation(toast, 1)).setInterpolator(Interpolator.OVERSHOOT);
        anim.setOnFinished(() -> toasting.remove(anim));
        toasting.add(anim);

        Platform.waitWhile(() -> toast.getHeight() == 0, () -> {
            if (!oldToasts.isEmpty()) {
                ParallelAnimation up = new ParallelAnimation(300).setInterpolator(Interpolator.OVERSHOOT);
                int y = toast.getHeight() + ContextUtils.dipToPx(20, this);
                for (View old : oldToasts) {
                    if (old != toast) {
                        up.addAnimation(new TranslateYAnimation(old, -y));
                        y += old.getHeight() + ContextUtils.dipToPx(20, this);
                    }
                }
                up.start();
            }
        });

        anim.start();

        Platform.runAfter(() -> Platform.waitWhile(() -> !toasting.isEmpty(), () -> {
            oldToasts.remove(toast);
            new ParallelAnimation(400).addAnimation(new AlphaAnimation(toast, 0)).addAnimation(new TranslateYAnimation(toast, 0).setLateTo(() -> toast.getTranslationY() - ContextUtils.dipToPx(60, this))).setInterpolator(Interpolator.EASE_OUT).setOnFinished(() -> root.removeView(toast)).start();
        }), duration);

        oldToasts.add(0, toast);
    }

    public void toast(String content, String... params) {
        toast(content, toastDur, params);
    }

    public void toast(String content, long dur, String... params) {
        Label lab = new ColoredLabel(this, Style.TEXT_NORM, content);
        lab.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        lab.setLineSpacing(10);
        lab.setFont(new Font(20));
        for (int i = 0; i < params.length; i++) {
            lab.addParam(i, params[i]);
        }
        toast(dur, lab);
    }

    public Insets getSystemInsets() {
        return systemInsets;
    }

    public void hideKeyboard() {
        View currentFocus = getWindow().getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
        }
    }

    public void showKeyboard(EditText input) {
        View currentFocus;
        if ((currentFocus = getWindow().getCurrentFocus()) != null) currentFocus.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    public void backPressed() {
        if (!loadedOverlay.isEmpty()) {
            loadedOverlay.get(0).back();
        } else if (loaded == null || !loaded.onBack()) {
            moveTaskToBack(true);
        }
    }

    public int getScreenHeight() {
        return ContextUtils.getScreenHeight(this);
    }

    public Property<Style> getStyle() {
        return style;
    }

    public void setBackgroundColor(int color) {
        Platform.runLater(() -> {
            int trans = adjustAlpha(color, 0.005f);
            Window win = getWindow();
            root.setBackgroundColor(color);
            win.setStatusBarColor(trans);
            win.setNavigationBarColor(trans);
            GradientDrawable b = new GradientDrawable();
            b.setColor(color);
            win.setBackgroundDrawable(b);
        });
    }

    public void applyTheme() {
        String theme = Store.getTheme();
        Style s = theme.equals(Style.THEME_DARK) ? dark : theme.equals(Style.THEME_LIGHT) ?
                light : isDarkMode(getResources().getConfiguration()) ? dark : light;

        if (s == style.get()) return;

        Style old = style.get();
        if (old != null) {
            if (theming != null && theming.isRunning()) theming.stop();
            Animation a = new ValueAnimation(400, 0, 1) {
                @Override
                public void updateValue(float v) {
                    style.set(Style.interpolateColors(old, s, v));
                }
            }.setOnFinished(() -> {
                ContextUtils.setStyle(this, s);
                setTheme(s.isDark() ? R.style.Theme_BondCheck_Dark : R.style.Theme_BondCheck_Light);
            }).setInterpolator(Interpolator.OVERSHOOT);
            theming = a;
            a.start();
        } else {
            ContextUtils.setStyle(this, s);
            setTheme(s.isDark() ? R.style.Theme_BondCheck_Dark : R.style.Theme_BondCheck_Light);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyTheme();
    }

    public void applyStyle(Style style) {
        if (style == null) return;
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(window, root);
        controllerCompat.setAppearanceLightStatusBars(style.isLight());
        controllerCompat.setAppearanceLightNavigationBars(style.isLight());

        setBackgroundColor(style.getBackgroundPrimary());
    }

    public boolean isDarkMode(Configuration newConfig) {
        return (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public Property<Locale> getLocale() {
        return locale;
    }

}
