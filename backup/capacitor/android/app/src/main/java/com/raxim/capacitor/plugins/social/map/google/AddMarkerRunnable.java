package com.raxim.capacitor.plugins.social.map.google;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.raxim.capacitor.utils.ConverterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddMarkerRunnable implements Runnable {
    private final ClusterManager clusterManager;
    private final PluginCall call;

    public AddMarkerRunnable(final PluginCall call, final ClusterManager clusterManager) {
        this.call = call;
        this.clusterManager = clusterManager;
    }

    @Override
    public void run() {
        JSObject data = call.getData();
        JSONArray markers = null;
        try {
            markers = data.getJSONArray("markers");
            for (int i = 0; i < markers.length(); i++) {
                JSONObject markerObject = markers.getJSONObject(i);
                String id = markerObject.getString("id");
                Double lat = markerObject.getDouble("lat");
                Double lng = markerObject.getDouble("lng");

                if (clusterManager != null) {
                    clusterManager.addItem(new MarkerItem(id, lat, lng));
                }
            }
            clusterManager.cluster();
        } catch (JSONException e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }
        //clusterManager.cluster();
        call.success();
    }
}
