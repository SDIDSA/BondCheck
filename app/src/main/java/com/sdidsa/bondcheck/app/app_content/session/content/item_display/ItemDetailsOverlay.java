package com.sdidsa.bondcheck.app.app_content.session.content.item_display;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.audio.AudioProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.location.AddressProxy;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.DateFormat;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.LocationOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.related.RelatedItemsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.overlays.ViewProfileOverlay;
import com.sdidsa.bondcheck.http.services.SessionService;
import com.sdidsa.bondcheck.models.responses.LocationResponse;
import com.sdidsa.bondcheck.models.responses.RecordResponse;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import java.util.ArrayList;
import java.util.Date;

public class ItemDetailsOverlay extends PartialSlideOverlay {
    private static final ArrayList<ItemDetailsOverlay> cache = new ArrayList<>();
    public static ItemDetailsOverlay getInstance(Context owner) {
        cache.removeIf(inst -> inst.getOwner() != owner);

        ItemDetailsOverlay found = null;
        for(ItemDetailsOverlay inst : cache) {
            if(!inst.isAttachedToWindow()) {
                found = inst;
                break;
            }
        }

        if(found == null) {
            found = new ItemDetailsOverlay(owner);
            cache.add(found);
        }

        return found;
    }

    private final ColoredLabel title;

    private final ItemDetail type;
    private final ItemDetail provider;
    private final ItemDetail location;
    private final ItemDetail app;
    private final ItemDetail duration;
    private final ItemDetail linked;
    private final ItemDetail time;
    private final ItemDetail edit;
    private final ItemDetail delete;

    private ItemDetailsOverlay(Context owner) {
        super(owner, -2);

        list.setPadding(20);
        list.setSpacing(20);

        title = new ColoredLabel(owner, Style.TEXT_NORM, "item_details")
                .setFont(new Font(20, FontWeight.MEDIUM));
        title.setMaxLines(1);

        ColoredIcon close = new ColoredIcon(owner, Style.TEXT_NORM, R.drawable.close, 42);
        close.setPadding(10);
        close.setOnClick(this::hide);

        HBox top = new HBox(owner);
        top.setAlignment(Alignment.CENTER_LEFT);
        top.addViews(title, SpacerUtils.spacer(owner, Orientation.HORIZONTAL),
                close);

        type = new ItemDetail(owner, "item_type", R.drawable.info);
        provider = new ItemDetail(owner, "item_provider", R.drawable.user_s);
        location = new ItemDetail(owner, "item_location", R.drawable.location_outline);
        app = new ItemDetail(owner, "screenshot_app", R.drawable.app);
        duration = new ItemDetail(owner, "record_duration", R.drawable.play);
        linked = new ItemDetail(owner, "related_events", R.drawable.linked);
        time = new ItemDetail(owner, "item_create_at", R.drawable.time);
        edit = new ItemDetail(owner, "edit_item", R.drawable.edit)
                .setSingleLine();
        delete = new ItemDetail(owner, "delete_item", R.drawable.delete)
                .setSingleLine()
                .setTextFill(Style.TEXT_ERR);

        linked.setKey("click_to_open");

        HBox bottom = new HBox(owner);
        MarginUtils.setMarginRight(edit, owner, 15);

        bottom.addViews(edit, delete);

        list.addViews(top, type, provider, app, duration, location, linked, time);

        list.addView(bottom);
    }

    private void load(Item item) {
        title.setKey(item.getType() + "_details");

        location.setVisibility(
                (item instanceof LocationResponse || !item.hasLocation()) ? GONE : VISIBLE
        );

        app.setVisibility(
                item instanceof ScreenshotResponse ? VISIBLE : GONE
        );

        edit.setVisibility(
                item.isEditable(Store.getUserId()) ? VISIBLE : GONE
        );

        delete.setVisibility(
                item.isDeletable(Store.getUserId()) ? VISIBLE : GONE
        );

        duration.setVisibility(
                item instanceof RecordResponse ? VISIBLE : GONE
        );

        type.setVisibility(VISIBLE);
        type.setIcon(item.getTypeIcon());
        type.setKey("item_type_" + item.getType());
        provider.setUser(item.provider());
        time.setDate(item.created_at());

        if(!item.hasLocation()) {
            location.setKey("Not available");
            location.setOnClickListener(null);
        } else {
            location.setText("");
            AddressProxy.getAddress(item.getLocation(), LocaleUtils.getLang(owner),
                    location::setText);
            location.setOnClickListener(e -> LocationOverlay.getInstance(owner).show(item));
        }
        app.setApp(item);
        duration.setDuration(item);

        linked.setVisibility(VISIBLE);
        linked.setOnClickListener(e -> RelatedItemsOverlay.getInstance(owner).show(item));
    }

    public void show(Item item) {
        super.show();
        load(item);
    }

    @Override
    public void show() {
        ErrorHandler.handle(new IllegalAccessError(
                        "can't show without loading an Item/Post object, " +
                                "use show(Item/Post) instead"),
                "showing ItemDetailsOverlay");
    }

    private static class ItemDetail extends ColoredHBox {
        private final ColoredLabel name;
        private final ColoredLabel value;
        private final ColoredIcon icon;
        private final ColoredIcon open;
        private final VBox content;
        public ItemDetail(Context owner, String nameString, @DrawableRes int iconRes) {
            super(owner, Style.EMPTY);
            setAlignment(Alignment.CENTER);

            content = new VBox(owner);
            content.setSpacing(4);
            content.setAlignment(Alignment.CENTER_LEFT);

            icon = new ColoredIcon(owner, Style.TEXT_SEC, iconRes, 27);
            icon.setAutoMirror(true);
            name = new ColoredLabel(owner, Style.TEXT_SEC, nameString)
                    .setFont(new Font(18));

            open = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.open, 28);
            open.setAutoMirror(true);
            open.setVisibility(INVISIBLE);
            open.setAlpha(.6f);

            value = new ColoredLabel(owner, Style.TEXT_NORM, "")
                    .setFont(new Font(18));

            content.addViews(name, value);

            MarginUtils.setMarginLeft(content, owner, 20);

            addView(icon);
            addView(content);
            addView(SpacerUtils.spacer(owner, Orientation.HORIZONTAL));
            addView(open);
        }

        @Override
        public void setOnClickListener(@Nullable OnClickListener l) {
            super.setOnClickListener(l);
            open.setVisibility(l == null ? INVISIBLE : VISIBLE);
        }

        public void setIcon(@DrawableRes int res) {
            icon.setImageResource(res);
        }

        public void setUser(String id) {
            setOnClickListener(null);
            value.setKey(null);
            SessionService.getUser(owner, id, user -> {
                value.setText(user.getUsername());
                setOnClickListener(e -> ViewProfileOverlay.getInstance(owner).show(id));
            });
        }

        public void setKey(String key) {
            value.setKey(key);
        }

        public void setDate(Date date) {
            value.setKey(null);
            value.setText(DateFormat.FULL_LONG.format(
                    LocaleUtils.getLocale(owner).get(),
                    DateFormat.convertToLocalDateTime(date)));
        }

        public void setText(String text) {
            value.setKey(null);
            value.setText(text);
        }

        public void setDuration(Item item) {
            value.setKey(null);
            if (item instanceof RecordResponse record) {
                AudioProxy.getAudio(owner, record.asset_id(), file -> {
                    int seconds = (int) ((file.duration() + 500) / 1000);
                    value.setKey("duration_seconds" + (seconds <= 10 ? "_sub_10" : ""),
                            Integer.toString(seconds));
                });
            } else {
                setText("");
            }
        }

        public ItemDetail setSingleLine() {
            name.setFont(new Font(18, FontWeight.MEDIUM));
            content.removeAllViews();
            removeAllViews();
            icon.setSize(22);
            removeView(content);
            addViews(name, icon);
            SpacerUtils.spacer(this, -2, -2);
            SpacerUtils.spacer(name);
            setPadding(15);
            setFill(Style.BACK_TER);
            setCornerRadius(15);
            return this;
        }

        public ItemDetail setTextFill(StyleToColor fill) {
            name.setFill(fill);
            icon.setFill(fill);

            return this;
        }

        public void setApp(Item item) {
            value.setKey(null);
            if(item instanceof ScreenshotResponse screen) {
                setText(screen.app());
            } else {
                setText("");
            }
        }
    }
}
