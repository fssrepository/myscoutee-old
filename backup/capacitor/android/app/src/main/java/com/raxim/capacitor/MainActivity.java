package com.raxim.capacitor;

import android.os.Bundle;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import com.raxim.capacitor.plugins.social.fcm.FCMPlugin;
import com.raxim.capacitor.plugins.social.map.google.GoogleMapPlugin;
import com.raxim.capacitor.plugins.social.mlvision.MLVisionPlugin;
import com.raxim.capacitor.plugins.social.mqtt.MQTTPlugin;
import com.raxim.capacitor.plugins.social.overlay.OverlayPlugin;

import java.util.ArrayList;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes the Bridge
        this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
            //add(MLVisionPlugin.class);
            add(GoogleMapPlugin.class);
            add(OverlayPlugin.class);
            //add(FCMPlugin.class);
            //add(MQTTPlugin.class);
        }});
    }

    //back button go back to the WebView history, needs to test
    @Override
    public void onBackPressed() {
        if (this.bridge == null) {
            return;
        }

        WebView webView = super.bridge.getWebView();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
