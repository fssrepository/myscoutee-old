package com.raxim.capacitor.plugins.social.overlay;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.raxim.capacitor.R;
import com.raxim.capacitor.utils.ConverterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

enum OVERLAY_TYPE {
    interactive,
    transparent
}

@NativePlugin()
public class OverlayPlugin extends Plugin {

    @PluginMethod()
    public void show(final PluginCall call) throws JSONException {
        float zoomScale = Resources.getSystem().getDisplayMetrics().density;

        JSObject data = call.getData();

        final List<RectF> overlayRects = new ArrayList<>();
        JSONArray overlays = data.getJSONArray("overlays");
        for (int i = 0; i < overlays.length(); i++) {
            overlayRects.add(ConverterUtils.convert(overlays.getJSONObject(i), zoomScale));
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView browserView = getActivity().findViewById(R.id.webview);
                browserView.setBackgroundColor(Color.TRANSPARENT);

                final ViewGroup rootView = (ViewGroup) browserView.getParent();

                browserView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        PointF pointF = new PointF(event.getX(), event.getY());
                        for (int i = rootView.getChildCount() - 2; i >= 0; i--) {

                            Object frameLayout = rootView.getChildAt(i);
                            if(frameLayout instanceof FrameLayout) {
                                RectF baseRect = ConverterUtils.locateView((FrameLayout)frameLayout);
                                if (baseRect.contains(pointF.x, pointF.y)) {
                                    for (RectF overlayRect : overlayRects) {
                                        if (overlayRect.contains(pointF.x, pointF.y)) {
                                            return false;
                                        }
                                    }

                                    ((FrameLayout)frameLayout).dispatchTouchEvent(event);
                                    break;
                                }
                            }
                        }

                        return false;
                    }
                });
                call.success();
            }

        });
    }

    @PluginMethod()
    public void hide(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView browserView = getActivity().findViewById(R.id.webview);
                browserView.setBackgroundColor(Color.WHITE);
                browserView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                call.success();
            }
        });
    }
}
