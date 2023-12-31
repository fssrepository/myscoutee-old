package com.raxim.capacitor.plugins.social.mlvision;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.raxim.capacitor.PermissionManager;
import com.raxim.capacitor.R;
import com.raxim.capacitor.plugins.social.mlvision.common.CameraSource;
import com.raxim.capacitor.plugins.social.mlvision.common.CameraSourcePreview;
import com.raxim.capacitor.plugins.social.mlvision.common.GraphicOverlay;
import com.raxim.capacitor.plugins.social.mlvision.common.VisionImageProcessor;
import com.raxim.capacitor.plugins.social.mlvision.common.processor.barcode.BarcodeScanningProcessor;
import com.raxim.capacitor.plugins.social.mlvision.common.processor.face.FaceDetectionProcessor;
import com.raxim.capacitor.utils.ConverterUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

enum MODEL_TYPE {
    barcode,
    face
}

enum CAMERA_TYPE {
    front,
    back
}

@NativePlugin()
public class MLVisionPlugin extends Plugin implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String ML_VISION = "ML_VISION";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    private VisionImageProcessor imageProcessor;

    private PermissionManager permissionManager;

    @PluginMethod()
    public void show(final PluginCall call) throws JSONException {

        permissionManager = new PermissionManager(getActivity());

        final String type = call.getString("type");

        float zoomScale = Resources.getSystem().getDisplayMetrics().density;
        JSObject data = call.getData();
        final JSONObject bound = data.getJSONObject("bound");
        final RectF boundRect = ConverterUtils.convert(bound, zoomScale);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final WebView browserView = getActivity().findViewById(R.id.webview);
                ViewGroup root = (ViewGroup) browserView.getParent();

                preview = new CameraSourcePreview(browserView.getContext());

                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) browserView.getLayoutParams();
                layoutParams.leftMargin = (int) boundRect.left;
                layoutParams.topMargin = (int) boundRect.top;
                layoutParams.width = (int) boundRect.right - (int) boundRect.left;
                layoutParams.height = (int) boundRect.bottom - (int) boundRect.top;
                preview.setLayoutParams(layoutParams);

                graphicOverlay = new GraphicOverlay(browserView.getContext());
                root.addView(preview);

        /*super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_live_preview);

        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        */

                //String type = call.getString("type");
                MODEL_TYPE model = MODEL_TYPE.valueOf(type);

                switch (model) {
                    case face:
                        imageProcessor = new FaceDetectionProcessor();
                        break;
                    case barcode:
                        imageProcessor = new BarcodeScanningProcessor();
                        break;
                    default:
                        Log.e(ML_VISION, "Unknown model: " + model);
                }

                String cameraType = "front";//call.getString("cameraType");
                if (cameraType != null) {
                    preview.stop();
                    if (permissionManager.allPermissionsGranted()) {
                        createCameraSource(type);
                        setCameraType(cameraType);
                        startCameraSource();
                        call.success();
                    } else {
                        permissionManager.getRuntimePermissions();
                        call.reject("No permission!");
                    }
                }
            }
        });
    }

    @PluginMethod()
    public void hide(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Boolean isDestroy = call.getBoolean("isDestroy");
                if (isDestroy != null && isDestroy == true) {
                    handleOnPause();
                    handleOnStop();
                    cameraSource = null;
                    preview = null;
                    graphicOverlay = null;
                    imageProcessor.stop();
                    imageProcessor = null;
                } else {
                    //hide
                }
            }
        });
    }

    @PluginMethod()
    public void verify(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = call.getString("data");

                String imageDataBytes = data.substring(data.indexOf(",") + 1);
                byte[] decodedString = Base64.decode(imageDataBytes, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                if (imageProcessor != null) {
                    imageProcessor.detectInImage(image).addOnSuccessListener(
                            new OnSuccessListener<List<Object>>() {
                                @Override
                                public void onSuccess(List<Object> results) {
                                    call.success();
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            call.reject(e.getMessage());
                                        }
                                    });
                }
            }
        });
    }

    private void setCameraType(String type) {
        CAMERA_TYPE cameraType = CAMERA_TYPE.valueOf(type);
        switch (cameraType) {
            case front:
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                break;
            case back:
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                break;
            default:
                Log.e(ML_VISION, "Unknown camera type: " + cameraType);
        }
    }

    @PluginMethod()
    public void switchCamera(PluginCall call) {
        if (cameraSource != null) {
            String cameraType = call.getString("cameraType");
            setCameraType(cameraType);
            preview.stop();
            startCameraSource();
            call.success();
        } else {
            call.reject("No camera!");
        }
    }

    @Override
    public void handleOnResume() {
        super.handleOnResume();
        Log.d(ML_VISION, "onResume");
        startCameraSource();
    }

    @Override
    protected void handleOnPause() {
        super.handleOnPause();
        preview.stop();
    }

    @Override
    public void handleOnStop() {
        super.handleOnStop();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private void createCameraSource(String model) {
        if (cameraSource == null) {
            cameraSource = new CameraSource(getActivity(), graphicOverlay);
        }

        MODEL_TYPE modelType = MODEL_TYPE.valueOf(model);
        switch (modelType) {
            case face:
                cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
                break;
            case barcode:
                cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor());
                break;
            default:
                Log.e(ML_VISION, "Unknown model: " + model);
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(ML_VISION, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(ML_VISION, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(ML_VISION, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
}
