package com.sdidsa.bondcheck.app.app_content.session.content.main.create.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.view.RotateAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.data.media.Media;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.overlays.CreateOverlay;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.requests.SavePostImageRequest;
import com.sdidsa.bondcheck.models.requests.SavePostRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;

public class PostButton extends ColoredIcon {
    private final CreateMain main;

    public PostButton(Context owner) {
        this(owner, null);
    }

    public PostButton(Context owner, CreateMain main) {
        super(owner, Style.WHITE, Style.ACCENT,
                R.drawable.send, 72);
        this.main = main;
        setPadding(15);
        setCornerRadius(72);
        MarginUtils.setMarginLeft(this, owner, 10);
        setAutoMirror(true);
        if (main != null) setOnClick(this::post);
    }

    private void post() {
        if (main.getHead().getValue().isBlank() && main.getImages().getMedia().isEmpty()) {
            ContextUtils.toast(owner, "Your post is empty...");
            return;
        }
        startLoading();
        Platform.runBack(() -> {
            List<Media> medias = main.getImages().getMedia();
            HashMap<Integer, String> indexedUrls = new HashMap<>();
            ArrayList<Thread> threads = getThreads(medias, indexedUrls);
            threads.forEach(Thread::start);
            threads.forEach(this::join);

            JSONArray arr = new JSONArray();
            indexedUrls.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .forEach(arr::put);
            System.out.println(arr);
            String other = Fragment.getInstance(owner, Main.class).getBondStatus().getOther_user();

            String content = main.getHead().getValue();
            SavePostRequest request = new SavePostRequest(other,
                    content.isBlank() ? null : content,
                    String.valueOf(main.getLocation()),
                    String.valueOf(main.getDetail()),
                    arr.length() == 0 ? null : arr.toString());

            Call<PostResponse> call = App.api(owner).savePost(request);
            Service.enqueue(call, resp -> {
                if (resp.isSuccessful()) {
                    assert resp.body() != null;
                    Platform.runLater(this::stopLoading);
                    Fragment.getInstance(owner, Main.class).addPost(resp.body());
                } else {
                    ContextUtils.toast(owner, "failed to save post");
                }
            });
        });
    }

    @NonNull
    private ArrayList<Thread> getThreads(List<Media> medias, HashMap<Integer, String> indexedUrls) {
        ArrayList<Thread> threads = new ArrayList<>();
        for(Media img : medias) {
            Thread th = new Thread(() -> {
                Bitmap bmp = ImageProxy.decode(owner, img.uri());
                File file = ImageProxy.saveTemp(bmp);
                SavePostImageRequest savePostImageRequest = new SavePostImageRequest(file);
                Call<GenericResponse> call = savePostImageRequest.savePostImage(owner);
                AtomicReference<String> url = new AtomicReference<>("");
                Semaphore mutex = new Semaphore(0);
                Service.enqueue(call, resp -> {
                    if (resp.isSuccessful() && resp.body() != null) {
                        url.set(resp.body().getMessage());
                        mutex.release();
                    } else {
                        ContextUtils.toast(owner, "problem_string");
                    }
                });
                mutex.acquireUninterruptibly();
                indexedUrls.put(medias.indexOf(img), url.get());
            });
            threads.add(th);
        }
        return threads;
    }

    Animation loading;

    private void startLoading() {
        if (loading == null) {
            loading = new RotateAnimation(2000, this, 0, 360);
            loading.setCycleCount(Animation.INDEFINITE);
        }
        enableDisableView(main, false);
        main.setAlpha(.5f);
        main.setEnabled(false);
        setImageResource(R.drawable.loading);
        loading.start();
    }

    private void stopLoading() {
        CreateOverlay cov = Fragment.getInstance(owner, Main.class).getCreateOverlay();
        cov.addOnHiddenOnce(() -> {
            loading.stop();
            setRotation(0);
            setImageResource(R.drawable.send);
            main.setAlpha(1f);
            main.reset();
            enableDisableView(main, true);
        });
        cov.hide();
    }

    private void join(Thread th) {
        try {
            th.join();
        } catch (InterruptedException e) {
            ErrorHandler.handle(e, "upload post images");
        }
    }

    public static void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup group) {
            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }
}
