package com.sdidsa.bondcheck.abs.components.layout.overlay.media.medialist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.data.media.Media;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaHolder> {
    private final Context owner;
    private List<Media> data;

    private final int size;

    private final Property<Media> selected;

    public MediaAdapter(Context owner, int size, Property<Media> selected) {
        this.owner = owner;
        this.selected = selected;
        this.data = new ArrayList<>();
        this.size = size;
    }

    @NonNull
    @Override
    public MediaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MediaHolder(new MediaEntry(owner, size / MediaList.SPAN_COUNT), selected);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaHolder holder, int position) {
        Media media = data.get(position);
        holder.load(media);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Media> data) {
        this.data = data;
        notifyDataSetChanged();

    }
}