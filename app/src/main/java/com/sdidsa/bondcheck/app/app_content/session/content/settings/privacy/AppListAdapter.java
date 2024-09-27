package com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdidsa.bondcheck.abs.components.controls.image.Image;
import com.sdidsa.bondcheck.abs.components.controls.input.checkBox.CheckBox;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private final Context owner;
    private final List<ResolveInfo> apps;
    private final PackageManager packageManager;
    private HashSet<String> selectedApps;

    public AppListAdapter(Context owner, List<ResolveInfo> apps, PackageManager packageManager) {
        this.owner = owner;
        this.apps = apps;
        this.packageManager = packageManager;
        this.selectedApps = new HashSet<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetList(ResolveInfo info) {
        int oldPos = apps.indexOf(info);
        Comparator<ResolveInfo> nameComparator = new ResolveInfo.DisplayNameComparator(packageManager);
        apps.sort((o1, o2) -> {
            if((selectedApps.contains(o1.activityInfo.packageName) && selectedApps.contains(o2.activityInfo.packageName))||
                    (!selectedApps.contains(o1.activityInfo.packageName) && !selectedApps.contains(o2.activityInfo.packageName))) {
                return nameComparator.compare(o1, o2);
            }else if(selectedApps.contains(o1.activityInfo.packageName)) {
                return -1;
            }else {
                return 1;
            }
        });
        if(info == null) {
            notifyDataSetChanged();
        } else {
            notifyItemMoved(oldPos, apps.indexOf(info));
        }
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppViewHolder(owner, new HBox(owner));
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ResolveInfo app = apps.get(position);
        holder.appName.setText(app.loadLabel(packageManager));
        holder.appIcon.setImageDrawable(app.loadIcon(packageManager));

        holder.appCheckbox.clearListeners();
        holder.appCheckbox.setChecked(selectedApps.contains(app.activityInfo.packageName));

        holder.appCheckbox.checkedProperty().addListener((ov, nv) -> {
            if(ov == nv) return;
            if (nv) {
                selectedApps.add(app.activityInfo.packageName);
            } else {
                selectedApps.remove(app.activityInfo.packageName);
            }
            resetList(app);
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public Set<String> getSelectedApps() {
        return selectedApps;
    }

    public void setSelectedApps(HashSet<String> selectedApps) {
        this.selectedApps = selectedApps;
        resetList(null);
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder implements Styleable {
        final Image appIcon;
        final Label appName;
        final CheckBox appCheckbox;

        public AppViewHolder(Context owner, HBox root) {
            super(root);
            appIcon = new Image(itemView.getContext());
            appName = new Label(itemView.getContext(), "");
            appCheckbox = new CheckBox(owner);

            ContextUtils.setPaddingVertical(root, 15, owner);

            appIcon.setSize(28);
            appName.setFont(new Font(22));
            appCheckbox.setSize(24);

            root.setOnClickListener(e -> appCheckbox.toggle());

            ContextUtils.setMarginRight(appIcon, owner, 15);

            root.setAlignment(Alignment.CENTER);

            root.addViews(appIcon, appName, ContextUtils.spacer(owner, Orientation.HORIZONTAL), appCheckbox);

            applyStyle(ContextUtils.getStyle(owner));
        }

        @Override
        public void applyStyle(Style style) {
            appName.setFill(style.getTextNormal());
        }

        @Override
        public void applyStyle(Property<Style> style) {
            Styleable.bindStyle(this, style);
        }
    }
}
