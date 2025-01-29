package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ScrollView;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.input.MinimalInputField;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.SpinLoading;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondStatus;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.UserCard;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.UserCardMode;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.util.List;

import retrofit2.Call;

public class MakeBondOverlay extends PartialSlideOverlay {
    private final BondStatus source;
    private final StackPane results;
    private final VBox users;
    private final ScrollView sv;
    private final Label empty;
    private final Label noRes;
    private final SpinLoading loading;
    private final MinimalInputField search;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long DEBOUNCE_DELAY = 500;

    public MakeBondOverlay(Context context) {
        this(context, null);
    }

    public MakeBondOverlay(Context owner, BondStatus source) {
        super(owner, .7f);
        this.source = source;

        search = new MinimalInputField(owner,
                "search_input");

        search.setFont(new Font(18));

        search.addPostInput(
                new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.search, 44)
                        .setImagePadding(10));

        search.valueProperty().addListener((ov, nv) -> {
            handler.removeCallbacksAndMessages(null);
            if (ov.equals(nv)) return;

            if (nv.length() > 2) {
                handler.postDelayed(() -> searchFor(nv), DEBOUNCE_DELAY);
            } else if (ov.length() > 2) {
                stopLoading(null);
            }
        });

        results = new StackPane(owner);
        SpacerUtils.spacer(results);
        results.getLayoutParams().width = LayoutParams.MATCH_PARENT;

        empty = new ColoredLabel(owner,
                Style.TEXT_SEC, "type_to_search"
        );
        empty.setFont(new Font(20));
        empty.centerText();

        noRes = new ColoredLabel(owner,
                Style.TEXT_SEC, "search_no_results"
        );
        noRes.setFont(new Font(20));
        noRes.centerText();

        users = new VBox(owner);
        users.setSpacing(20);

        sv = new ScrollView(owner);
        sv.addView(users);

        results.setClipToOutline(false);
        results.setClipToPadding(false);
        results.setClipChildren(false);

        loading = new ColoredSpinLoading(owner, Style.TEXT_SEC, 48);

        addOnShown(() -> {
            search.getInput().requestFocus();
            ContextUtils.showKeyboard(owner, search.getInput());
        });

        stopLoading(null);

        addOnHidden(() -> {
            ContextUtils.hideKeyboard(owner);
            handler.removeCallbacksAndMessages(null);
        });

        addOnShowing(() -> search.setValue(""));

        list.setPadding(20);
        list.setSpacing(30);
        list.addView(search);
        list.addView(results);
    }

    private void startLoading() {
        results.removeAllViews();
        results.addCentered(loading);
        loading.startLoading();
    }

    private void stopLoading(List<UserResponse> userList) {
        loading.stopLoading();
        results.removeAllViews();
        users.removeAllViews();
        if(userList == null) {
            results.addCentered(empty);
        }else {
            userList.removeIf(user -> user.getId().equals(Store.getUserId()));
            if(userList.isEmpty()) {
                results.addCentered(noRes);
            }else {
                for(UserResponse user : userList) {
                    users.addView(UserCard.make(owner, source, user,
                            UserCardMode.SEND_MODE));
                }
                results.addView(sv);
            }
        }
    }

    private long lastCommand = -1;
    private void searchFor(String query) {
        final long thisCommand = System.currentTimeMillis();

        lastCommand = thisCommand;
        startLoading();

        Call<List<UserResponse>> call = App.api(owner).searchUsers(
                new StringRequest(query));

        Service.enqueue(call, resp -> {
            if(lastCommand != thisCommand || !search.getValue().equals(query)) {
                return;
            }

            if(resp.isSuccessful()) {
                stopLoading(resp.body());
            }else {
                stopLoading(null);
                ContextUtils.toast(owner, "problem_string");
            }
        });
    }

    @Override
    public void applySystemInsets(Insets insets) {
        super.applySystemInsets(insets);
        setHeight((int) ((ContextUtils.getScreenHeight(owner) - insets.top - insets.bottom)
                * 0.7));
    }
}
