package com.here.gistec.tilemapservice;

import com.here.android.mpa.mapping.MapOverlayType;
import com.here.android.mpa.mapping.UrlMapRasterTileSourceBase;

/**
 * Created by ZAD on 12/20/2016.
 */

public class LiveMapRasterTileSource extends UrlMapRasterTileSourceBase {

    private final static String URL_FORMAT =
            "http://tile.openstreetmap.com/{z}/{x}/{y}.png";

    public LiveMapRasterTileSource() {
        // We want the tiles placed over everything else
        setOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
        // We don't want the map visible beneath the tiles
        setTransparency(Transparency.OFF);
        // We don't want the tiles visible between these zoom levels
        hideAtZoomRange(12, 20);
        // Do not cache tiles
        setCachingEnabled(false);
    }

    // Implementation of UrlMapRasterTileSourceBase
    public String getUrl(int x, int y, int zoomLevel) {
        String url = URL_FORMAT;

        url = url.replace("{z}", "" + zoomLevel);
        url = url.replace("{x}", "" + x);
        url = url.replace("{y}", "" + y);

        return url;
    }
}