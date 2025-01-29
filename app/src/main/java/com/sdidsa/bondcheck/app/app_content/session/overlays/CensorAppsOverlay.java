package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredLinearLoading;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.ColoredHBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.PaddingUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy.AppListAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class CensorAppsOverlay extends PartialSlideOverlay {
    public CensorAppsOverlay(Context owner) {
        super(owner, .7f);

        list.setAlignment(Alignment.CENTER);

        ColoredLinearLoading loading = new ColoredLinearLoading(owner, Style.TEXT_SEC, 15);
        loading.startLoading();
        list.addView(loading);

        addOnShownOnce(() ->
                Platform.runBack(() -> {
                    PackageManager pm = owner.getPackageManager();

                    List<ResolveInfo> launchables = getAppsList(pm);
                    launchables.sort(new ResolveInfo.DisplayNameComparator(pm));
                    RecyclerView recyclerView = new RecyclerView(owner);
                    recyclerView.setLayoutManager(new LinearLayoutManager(owner));

                    AppListAdapter adapter = new AppListAdapter(owner, launchables, pm);
                    recyclerView.setAdapter(adapter);

                    adapter.setSelectedApps(Store.getCensoredApps());

                    ColoredLabel lab = new ColoredLabel(owner, Style.TEXT_SEC, "censor_apps_hint");
                    lab.setFont(new Font(20));
                    lab.centerText();
                    lab.setLineSpacing(5);

                    ColoredStackPane sp = new ColoredStackPane(owner, Style.BACK_PRI);
                    sp.addCentered(lab);
                    PaddingUtils.setPaddingVertical(sp, 20, owner);
                    sp.setElevation(2000);

                    Button ready = new ColoredButton(owner, Style.ACCENT, s -> Color.WHITE, "save");
                    ready.setFont(new Font(18, FontWeight.MEDIUM));
                    ready.setElevation(0);

                    ready.setOnClick(() -> Store.setCensoredApps(adapter.getSelectedApps(), r -> hide()));

                    Button cancel = new ColoredButton(owner, Style.BACK_SEC,
                            Style.TEXT_NORM, "cancel");
                    cancel.setFont(new Font(18, FontWeight.MEDIUM));
                    cancel.setElevation(0);

                    cancel.setOnClick(this::hide);

                    addOnHidden(() -> adapter.setSelectedApps(Store.getCensoredApps()));

                    ColoredHBox buttons = new ColoredHBox(owner, Style.BACK_PRI);
                    PaddingUtils.setPaddingVertical(buttons, 20, owner);
                    buttons.addViews(cancel, ready);
                    SpacerUtils.spacer(ready);
                    SpacerUtils.spacer(cancel);
                    SpacerUtils.spacer(recyclerView);

                    MarginUtils.setMarginLeft(ready, owner, 15);

                    Platform.runLater(() -> {
                        list.removeView(loading);
                        list.setAlignment(Alignment.TOP_CENTER);
                        PaddingUtils.setPadding(list, 20,0,20,0, owner);
                        list.addView(sp);
                        list.addView(recyclerView);
                        list.addView(buttons);
                    });
        }));
    }

    public List<ResolveInfo> getAppsList(PackageManager pm) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        return pm.queryIntentActivities(intent, 0).stream().filter(app -> {
          try {
              app.loadLabel(pm);
              return true;
          }catch(Exception x) {
              return false;
          }
        }).collect(Collectors.toList());
    }
}
