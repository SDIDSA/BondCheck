package com.sdidsa.bondcheck.models.responses;

import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;

import java.util.ArrayList;
import java.util.List;

public class RelatedItemsResponse {
    private final List<RecordResponse> records;
    private final List<ScreenshotResponse> screens;
    private final List<LocationResponse> locations;

    public RelatedItemsResponse(List<RecordResponse> records,
                                List<ScreenshotResponse> screens,
                                List<LocationResponse> locations) {
        this.records = records;
        this.screens = screens;
        this.locations = locations;
    }

    public List<Item> getCombined() {
        List<Item> combined = new ArrayList<>();
        combined.addAll(records);
        combined.addAll(screens);
        combined.addAll(locations);
        return combined;
    }
}
