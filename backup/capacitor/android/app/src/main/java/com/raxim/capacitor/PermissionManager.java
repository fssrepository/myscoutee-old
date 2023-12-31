package com.raxim.capacitor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String PERMISSION_MANAGER = "PERMISSION_MANAGER";
    private static final int PERMISSION_REQUESTS = 1;

    private final AppCompatActivity activity;

    public PermissionManager(final AppCompatActivity activity) {
        this.activity = activity;
    }

    public void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(activity, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    activity.getPackageManager()
                            .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    public boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(activity, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(PERMISSION_MANAGER, "Permission granted!");
        if (allPermissionsGranted()) {
            //createCameraSource(selectedModel);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(PERMISSION_MANAGER, "Permission granted: " + permission);
            return true;
        }
        Log.i(PERMISSION_MANAGER, "Permission NOT granted: " + permission);
        return false;
    }

}
