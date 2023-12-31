package com.raxim.capacitor.plugins.social.map.google;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerItem implements ClusterItem {
    private final String id;
    private final LatLng mPosition;
    private final String mTitle;
    private final String mSnippet;

    public MarkerItem(String id, double lat, double lng) {
        this(id, lat, lng, null, null);
    }

    public MarkerItem(String id, double lat, double lng, String title, String snippet) {
        this.id = id;
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public String getId() {
        return id;
    }
}
