package com.sdidsa.bondcheck.app.app_content.session.content.main.create.location;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.input.MinimalInputField;
import com.sdidsa.bondcheck.abs.components.controls.location.AddressProxy;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.SpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.scroll.Recycler;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.LocationUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.CreateFragment;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.main.CreateMain;
import com.sdidsa.bondcheck.models.DBLocation;

import java.util.Arrays;
import java.util.List;

public class LocationFragment extends CreateFragment {
    private final StackPane results;
    private final Recycler<LocationDetail, LocationDisplay> sv;
    private final Label empty;
    private final Label noRes;
    private final SpinLoading loading;

    private final MinimalInputField search;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long DEBOUNCE_DELAY = 500;

    public LocationFragment(Context owner) {
        super(owner);
        setPadding(15);
        setSpacing(10);

        ColoredIcon back = new ColoredIcon(owner, Style.TEXT_SEC,
                com.sdidsa.bondcheck.R.drawable.arrow_left, 48);
        back.setAutoMirror(true);
        back.setPadding(12);
        back.setOnClick(() -> getPane().previousInto(CreateMain.class));
        MarginUtils.setMarginRight(back, owner, 10);

        ColoredLabel head = new ColoredLabel(owner, Style.TEXT_NORM, "create_location_header")
                .setFont(new Font(22, FontWeight.MEDIUM));

        HBox top = new HBox(owner);
        top.setAlignment(Alignment.CENTER_LEFT);
        top.addViews(back, head);

        search = new MinimalInputField(owner,
                "create_location_header");
        search.setBackFill(Style.BACK_PRI);
        search.setFont(new Font(18));

        ColoredIcon locate = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.location_fill, 44)
                .setImagePadding(10);

        locate.setOnClick(() -> {
            search.setValue("");
            search.setEditable(false);
            startLoading();
            Platform.runBack(() -> {
                DBLocation location = LocationUtils.getLocation(owner);
                Platform.runLater(() -> {
                    if (location != null) {
                        AddressProxy.getAddress(location,
                                LocaleUtils.getLocale(owner).get().getLang(), v -> {
                                    LocationDetail det = new LocationDetail(LocationType.LOCALITY, v, location);
                                    stopLoading(det);
                                });
                    } else {
                        ContextUtils.toast(owner, "problem_string");
                        stopLoading();
                    }
                    search.setEditable(true);
                });
            });
        });

        search.addPostInput(locate);
        search.addPostInput(
                new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.search, 44)
                        .setImagePadding(10));

        search.valueProperty().addListener((ov, nv) -> {
            handler.removeCallbacksAndMessages(null);
            if (ov.equals(nv)) return;

            if (nv.length() > 2) {
                handler.postDelayed(() -> searchFor(nv), DEBOUNCE_DELAY);
            } else if (ov.length() > 2) {
                stopLoading((List<LocationDetail>) null);
            }
        });

        results = new StackPane(owner);
        SpacerUtils.spacer(results);
        results.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;

        empty = new ColoredLabel(owner,
                Style.TEXT_SEC, "location_type_to_search"
        );
        empty.setFont(new Font(20));
        empty.centerText();

        noRes = new ColoredLabel(owner,
                Style.TEXT_SEC, "no_location_results"
        );
        noRes.setFont(new Font(20));
        noRes.centerText();

        sv = new Recycler<>(owner, LocationDisplay.class);
        sv.setLayoutParams(new LayoutParams(-1, -1));
        sv.setClipToOutline(true);

        results.setClipToOutline(false);
        results.setClipToPadding(false);
        results.setClipChildren(false);

        loading = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);

        stopLoading((List<LocationDetail>) null);

        addView(top);
        addView(search);
        addView(results);
    }

    private void startLoading() {
        if (!loading.isRunning()) {
            results.removeAllViews();
            results.addCentered(loading);
            loading.startLoading();
        }
    }

    private void stopLoading(List<LocationDetail> locationList) {
        loading.stopLoading();
        results.removeAllViews();
        sv.clearItems();
        if (locationList == null) {
            results.addCentered(empty);
        } else {
            if (locationList.isEmpty()) {
                results.addCentered(noRes);
            } else {
                sv.setItems(locationList);
                results.addView(sv);
            }
        }
    }

    private void stopLoading(LocationDetail... locations) {
        stopLoading(Arrays.asList(locations));
    }

    private long lastCommand = -1;

    private void searchFor(String query) {
        final long thisCommand = System.currentTimeMillis();

        lastCommand = thisCommand;
        startLoading();

        LocationSearch.searchLocation(query, LocaleUtils.getLang(owner),
                resp -> {
                    if (lastCommand != thisCommand || !search.getValue().equals(query)) {
                        return;
                    }
                    stopLoading(resp);
                },
                e -> ContextUtils.toast(owner, "problem_string"));
    }

    public void reset() {
        sv.scrollTo(0, 0);
        search.setValue("");
    }
}
