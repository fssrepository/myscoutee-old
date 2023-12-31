package com.raxim.capacitor.plugins.social.map.google;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginRequestCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.raxim.capacitor.R;
import com.raxim.capacitor.utils.ConverterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@NativePlugin()
public class GoogleMapPlugin extends Plugin implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String MARKER_CLICK_LISTENER = "MARKER_CLICK_LISTENER";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private GoogleMap mMap;
    private MapView mapView;

    private ClusterManager clusterManager;
    private LocationManager locationManager;

    @PluginMethod()
    public void addMarkers(final PluginCall call) throws JSONException {
        if (clusterManager != null) {
            getActivity().runOnUiThread(new AddMarkerRunnable(call, clusterManager));
        } else {
            call.reject("no cluster manager");
        }
    }

    private void setLayoutParams(final RectF boundRect) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mapView.getLayoutParams();
        layoutParams.leftMargin = (int) boundRect.left;
        layoutParams.topMargin = (int) boundRect.top;
        layoutParams.width = (int) boundRect.right - (int) boundRect.left;
        layoutParams.height = (int) boundRect.bottom - (int) boundRect.top;
        mapView.setLayoutParams(layoutParams);
    }

    @PluginMethod()
    public void show(final PluginCall call) throws JSONException {
        float zoomScale = Resources.getSystem().getDisplayMetrics().density;

        JSObject data = call.getData();

        final JSONObject bound = data.getJSONObject("bound");
        final RectF boundRect = ConverterUtils.convert(bound, zoomScale);

        if(mapView != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setLayoutParams(boundRect);
                    mapView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final WebView browserView = getActivity().findViewById(R.id.webview);
                    ViewGroup root = (ViewGroup) browserView.getParent();

                    mapView = new MapView(browserView.getContext());
                    mapView.onCreate(null);
                    root.addView(mapView, 0);

                    setLayoutParams(boundRect);

                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap = googleMap;

                            //TODO: after permission request and reload the map
                            locationManager = (LocationManager) browserView.getContext().getSystemService(Context.LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(browserView.getContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(browserView.getContext(),
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                                ActivityCompat.requestPermissions(getActivity(), permissions, PluginRequestCodes.GEOLOCATION_REQUEST_PERMISSIONS);
                                return;
                            }

                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                                    mMap.animateCamera(cameraUpdate);
                                    locationManager.removeUpdates(this);
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {
                                }

                                @Override
                                public void onProviderEnabled(String provider) {
                                }

                                @Override
                                public void onProviderDisabled(String provider) {
                                }
                            });

                            clusterManager = new ClusterManager(browserView.getContext(), mMap);
                            mMap.setOnCameraIdleListener(clusterManager);
                            mMap.setOnMarkerClickListener(clusterManager);

                            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener() {
                                @Override
                                public boolean onClusterItemClick(ClusterItem clusterItem) {
                                    if (clusterItem instanceof MarkerItem) {
                                        JSObject markerObject = new JSObject();
                                        markerObject.put("id", ((MarkerItem) clusterItem).getId());
                                        notifyListeners(MARKER_CLICK_LISTENER, markerObject);
                                        return true;
                                    }
                                    return false;
                                }
                            });

                            mapView.onResume();
                            call.success();
                        }
                    });
                }
            });
        }
    }

    @PluginMethod()
    public void hide(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Boolean isDestroy = call.getBoolean("isDestroy");
                if (isDestroy != null && isDestroy == true) {
                    WebView browserView = getActivity().findViewById(R.id.webview);
                    ViewGroup root = (ViewGroup) browserView.getParent();
                    root.removeView(mapView);
                    mapView.onDestroy();
                    mMap.clear();
                    mapView = null;
                    mMap = null;
                } else {
                    mapView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //needs to be implemented
    }
}
