package com.raxim.capacitor.utils;

import android.graphics.Rect;
import android.graphics.RectF;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class ConverterUtils {
    public static RectF convert(JSONObject jsonObject, float zoomScale) throws JSONException {
        float left = Float.parseFloat(jsonObject.getString("left")) * zoomScale;
        float right = Float.parseFloat(jsonObject.getString("right")) * zoomScale;
        float top = Float.parseFloat(jsonObject.getString("top")) * zoomScale;
        float bottom = Float.parseFloat(jsonObject.getString("bottom")) * zoomScale;
        return new RectF(left, top, right, bottom);
    }

    public static MarkerOptions convert(JSONObject jsonObject) throws JSONException {
        Double lat = jsonObject.getDouble("lat");
        Double lng = jsonObject.getDouble("lng");
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        return markerOptions;
    }

    public static RectF locateView(View view) {
        RectF loc = new RectF();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();

        loc.left = layoutParams.leftMargin;
        loc.top = layoutParams.topMargin;
        loc.right = layoutParams.leftMargin + layoutParams.width;
        loc.bottom = layoutParams.topMargin + layoutParams.height;
        return loc;
    }
}
