package com.sdidsa.bondcheck.abs.components.layout.overlay.media.medialist;

import androidx.recyclerview.widget.RecyclerView;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.data.media.Media;

public class MediaHolder extends RecyclerView.ViewHolder {
    private final MediaEntry entry;
    private Media data;
    private final Property<Media> selected;

    public MediaHolder(MediaEntry entry, Property<Media> selected) {
        super(entry);
        this.selected = selected;
        entry.setOnClick(() -> {
            if (data == selected.get()) {
                selected.set(null);
            } else {
                selected.set(data);
            }
        });
        selected.addListener((ov, nv) -> {
            if(ov == data && nv != data) {
                entry.deselect();
            }
            if(ov != data && nv == data) {
                entry.select();
            }
        });
        this.entry = entry;
    }

    public void load(Media data) {
        if (selected.get() == data && selected.get() != this.data) {
            entry.select();
        } else if (selected.get() != data && selected.get() == this.data) {
            entry.deselect();
        }
        this.data = data;
        entry.load(data);
    }
}