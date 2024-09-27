package com.sdidsa.bondcheck.app.services;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sdidsa.bondcheck.abs.utils.Store;

public class TransparentActivity extends AppCompatActivity {
    private Runnable onCaptureRejected;
    private String requester;
    private final int REQUEST_CODE_CAPTURE_PERM = 2048;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowAttributes();
        Store.init(this);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> onActivityResult(REQUEST_CODE_CAPTURE_PERM,
                        result.getResultCode(),
                        result.getData())
        );

        String requester = getIntent().getStringExtra("requester");

        captureScreen(requester, () -> Log.i("screenshot", "rejected"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("activity", "resumed");
    }

    @Override
    public void finish() {
        super.finish();
        moveTaskToBack(true);
    }

    public void captureScreen(String requester, Runnable onRejected) {
        Log.i("socket", "capturing");
        onCaptureRejected = onRejected;
        this.requester = requester;
        if (!ScreenCaptureService.isServiceRunning()) {
            requestScreenCapturePermission();
        }else {
            ScreenCaptureService.requestCapture(this, requester);
            finish();
        }
    }

    private void setWindowAttributes() {
        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT);

        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void requestScreenCapturePermission() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        captureIntent.putExtra("request_code", REQUEST_CODE_CAPTURE_PERM);
        activityResultLauncher.launch(captureIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i("activity result", "code " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_PERM) {
            if (resultCode == RESULT_OK) {
                Log.i("capture permission", "accepted");
                Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                serviceIntent.putExtra("resultCode", resultCode);
                serviceIntent.putExtra("data", data);
                startForegroundService(serviceIntent);
                ScreenCaptureService.requestCapture(this, requester);
            } else {
                if (onCaptureRejected != null) onCaptureRejected.run();
            }
            finish();
        }
    }
}