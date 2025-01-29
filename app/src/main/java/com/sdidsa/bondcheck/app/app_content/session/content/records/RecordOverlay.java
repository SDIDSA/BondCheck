package com.sdidsa.bondcheck.app.app_content.session.content.records;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.UiCache;
import com.sdidsa.bondcheck.abs.components.controls.audio.AudioProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.abs.CornerUtils;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemDetailsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlayHeader;
import com.sdidsa.bondcheck.models.responses.RecordResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordOverlay extends PartialSlideOverlay implements ItemOverlay {
    private static final ArrayList<RecordOverlay> cache = new ArrayList<>();
    public static RecordOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        RecordOverlay found = null;
        for(RecordOverlay inst : cache) {
            if(!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if(found == null) {
            found = new RecordOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    public static void clearCache() {
        cache.clear();
    }

    static {
        UiCache.register(RecordOverlay::clearCache);
    }

    private RecordResponse record;
    private File file;
    private final ColoredIcon play;
    private MediaPlayer mediaPlayer;
    private boolean playing;
    private final PlayerProgress progress;
    private final Handler handler;
    private Runnable updateProgressAction;
    private boolean wasPlaying = false;

    private final VBox root;
    private final ColoredSpinLoading loader;

    //labels
    private final ColoredLabel current;
    private final ColoredLabel total;
    private final ColoredLabel speed;

    private final ItemOverlayHeader header;

    private RecordOverlay(Context owner) {
        super(owner, .4f);

        list.setAlignment(Alignment.TOP_CENTER);
        list.setPadding(20);
        list.setSpacing(30);
        list.setClipChildren(false);

        root = new VBox(owner);
        root.setAlignment(Alignment.BOTTOM_CENTER);
        root.setSpacing(50);

        loader = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);

        header = new ItemOverlayHeader(owner);
        header.setOnClose(this::hide);
        header.setOnSave(this::saveAudio);
        header.setOnInfo(() -> ItemDetailsOverlay.getInstance(owner).show(record));

        StackPane controls = new StackPane(owner);

        play = new ColoredIcon(owner, Style.BACK_PRI, Style.TEXT_NORM,
                R.drawable.play, 64);
        play.setPadding(4);
        play.setRadiusNoClip(64);
        play.setImagePadding(15);
        play.setAutoMirror(true);
        play.setElevation(SizeUtils.dipToPx(7, owner));

        speed = new ColoredLabel(owner, Style.BACK_PRI, Style.TEXT_SEC, "x1.0");
        speed.setLayoutParams(new LinearLayout.LayoutParams(
                SizeUtils.dipToPx(57, owner),
                LayoutParams.WRAP_CONTENT
        ));
        speed.setTranslationX(SizeUtils.dipToPx(54, owner));
        speed.setFont(new Font(16, FontWeight.BOLD));
        speed.centerText();
        PaddingUtils.setPaddingHorizontalVertical(speed, 0, 7, owner);
        speed.setCornerRadius(CornerUtils.cornerRightRadius(owner, 30));

        speed.setOnClickListener((e) -> {
            double ov = Double.parseDouble(speed.getText().toString().substring(1));
            double nv = ov + 0.5;
            nv = nv > 2 ? 0.5 : nv;
            speed.setText("x".concat(Double.toString(nv)));
            if(mediaPlayer != null) {
                wasPlaying = mediaPlayer.isPlaying();
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) nv);
                mediaPlayer.setPlaybackParams(playbackParams);
                if(wasPlaying) play();
                else pause();
            }
        });

        current = new ColoredLabel(owner, Style.TEXT_SEC ,"00:00")
                .setFont(new Font(16, FontWeight.MEDIUM));
        total = new ColoredLabel(owner, Style.TEXT_SEC ,"00:00")
                .setFont(new Font(16, FontWeight.MEDIUM));

        StackPane times = new StackPane(owner);
        times.addAligned(current, Alignment.BOTTOM_LEFT);
        times.addAligned(total, Alignment.BOTTOM_RIGHT);

        times.setTranslationY(-SizeUtils.dipToPx(40, owner));

        controls.addCentered(play);
        controls.addCentered(speed);

        progress = new PlayerProgress(owner);
        progress.addCentered(times);
        MarginUtils.setMarginBottom(progress, owner, 30);

        progress.setOnStartSeeking(() -> {
            wasPlaying = false;
            if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                wasPlaying = true;
                pause();
            }
        });

        progress.setOnSeek(p -> {
            int duration = mediaPlayer.getDuration();
            int currentPosition = (int) (duration * p);
            seekTo(currentPosition);

            if(wasPlaying) {
                play();
            }
        });

        progress.setOnSeeking(p -> {
            int duration = mediaPlayer.getDuration();
            int currentPosition = (int) (duration * p);

            updateTime(current, currentPosition);
        });

        handler = new Handler(Looper.getMainLooper());
        updateProgressAction = () -> {
            if (mediaPlayer != null) {
                if(mediaPlayer.isPlaying()) {
                    louder.setEnabled(true);
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();

                    float p = (float) currentPosition / duration;
                    progress.setProgress(p);

                    updateTime(current, currentPosition);
                }
                handler.postDelayed(updateProgressAction, 10);
            }
        };

        root.addView(controls);
        root.addView(progress);

        SpacerUtils.spacer(root, Orientation.VERTICAL);
        MarginUtils.setMarginTop(loader, owner, 64);

        list.addView(header);
        list.addView(root);
    }

    private LoudnessEnhancer louder;
    @SuppressLint("SetTextI18n")
    private void load(RecordResponse record) {
        this.record = record;
        header.setUser(record.provider());
        header.setTitle("");
        playing = false;
        progress.setProgress(0);
        play.setImageResource(R.drawable.play);
        speed.setText("x1.0");
        AudioProxy.getAudio(owner, record.asset_id(), file -> {
            this.file = file.file();
            int seconds = (int) ((file.duration() + 500) / 1000);
            header.setTitle("duration_seconds" + (seconds <= 10 ? "_sub_10" : ""),
                    Integer.toString(seconds));
            Platform.runBack(() -> {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(file.file().getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.setVolume(1,1);
                    updateTime(total, mediaPlayer.getDuration());
                    updateTime(current, 0);
                    handler.post(updateProgressAction);
                    mediaPlayer.setOnCompletionListener((player) -> {
                        pause();
                        seekTo(0);
                        progress.setProgress(0);
                        playing = false;
                    });

                    play.setOnClick(() -> {
                        if(playing) {
                            pause();
                        }else {
                            play();
                        }
                    });

                    float tooLow = -40;
                    float tooHigh = -8f;

                    float vol = file.loudness();
                    if (Float.isNaN(vol) || !Float.isFinite(vol)) {
                        vol = -20;
                    }
                    float gainDb = tooHigh - vol;

                    int gainMb = (int) (gainDb * 100);
                    louder =
                            new LoudnessEnhancer(mediaPlayer.getAudioSessionId());
                    louder.setTargetGain(vol < tooLow || vol > tooHigh ? 0 : gainMb);
                    louder.setEnabled(true);

                    Platform.runLater(() -> {
                        loader.stopLoading();
                        list.removeView(loader);
                        list.addView(root);
                    });
                } catch (IOException e) {
                    ErrorHandler.handle(e, "opening audio file");
                }
            });
        });
    }

    private void seekTo(int seekTo) {
        updateTime(current, seekTo);
        mediaPlayer.seekTo(seekTo);
    }

    @SuppressLint("SetTextI18n")
    private void updateTime(ColoredLabel lab, int time) {
        int durSecTotal = (time + (lab == total ? 500 : 0)) / 1000;
        int durSec = durSecTotal % 60;
        int durMin = durSecTotal / 60;
        lab.setText(format(durMin) + ":" + format(durSec));
    }

    private String format(int v) {
        return (v < 10 ? "0":"") + v;
    }

    public void pause() {
        mediaPlayer.pause();
        play.setImageResource(R.drawable.play);

        playing = false;
    }

    public void play() {
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause);

        playing = true;
    }

    public void show(Item item) {
        show(item, false);
    }

    public void show(Item item, boolean related) {
        if(item instanceof RecordResponse data) {
            header.showInfo(!related);
            list.removeView(root);
            loader.startLoading();
            list.addView(loader);
            load(data);
            super.show();
        } else {
            ErrorHandler.handle(
                    new IllegalArgumentException("wrong item type, should be RecordResponse"),
                    "loading RecordOverlay");
        }
    }

    @Override
    public void show() {
        ErrorHandler.handle(new IllegalAccessError(
                        "can't show without loading a record object, " +
                                "use show(RecordResponse) instead"),
                "showing RecordOverlay");
    }

    @Override
    public void hide() {
        super.hide();

        handler.removeCallbacks(updateProgressAction);

        Platform.runBack(() -> {
            if(mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
    }

    private void saveAudio() {
        if(file == null) {
            ContextUtils.toast(owner, "still loading...");
        }
        int res = AudioProxy.saveAudioToGallery(owner, file,
                ContextUtils.getAppName(owner).toLowerCase() +
                        "_" + record.id() + ".mp3");

        switch (res) {
            case ImageProxy.FILE_SAVED ->
                    ContextUtils.toast(owner, "Audio saved");
            case ImageProxy.FILE_EXISTS ->
                    ContextUtils.toast(owner,
                            "This audio has already been saved");
            case ImageProxy.FILE_ERROR ->
                    ContextUtils.toast(owner,
                            "Something went wrong, retry later");
        }
    }
}
