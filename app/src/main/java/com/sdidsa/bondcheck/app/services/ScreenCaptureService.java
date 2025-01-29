package com.sdidsa.bondcheck.app.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.LocationUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy.PrivacyGroup;
import com.sdidsa.bondcheck.app.app_content.session.permission.PermissionCheck;
import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.requests.SaveItemRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import java.util.List;

import retrofit2.Call;

public class ScreenCaptureService extends Service {
    private static final int ID = 2048;
    private static final String CHANNEL_ID = "screen_capture";
    static long lastCaptureTime = 0;
    private static boolean isRunning = false;
    private static ImageReader imageReader;
    BroadcastListener receiver;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjection.Callback projectionCallback;

    public static boolean isServiceRunning() {
        return isRunning;
    }

    public static void requestCapture(Context context, String requester) {
        Platform.runAfter(() -> {
            Intent request = App.broadcast(Action.REQUEST_SCREEN);
            request.putExtra("requester", requester);
            context.sendBroadcast(request);
        }, 500);
    }

    private static String getAppName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            return START_STICKY;
        }
        if ("STOP_SERVICE".equals(intent.getAction())) {
            suicide();
            return START_STICKY;
        }

        receiver = new BroadcastListener(this);

        receiver.on(Action.STOP_SCREEN_SHARING, this::suicide);

        receiver.onIntent(Action.REQUEST_SCREEN, data ->
                Platform.runAfter(() -> {
                    if (imageReader != null) {
                        Image image = imageReader.acquireLatestImage();
                        Bitmap bitmap = ScreenshotUtils.imageToBitmap(this, image);
                        image.close();
                        Platform.runBack(() -> {
                            if (Store.getScreenConsent().equals(PrivacyGroup.ASK_EVERY_TIME)) {
                                suicide();
                            }
                            String requester = data.getStringExtra("requester");
                            processBitmap(this, bitmap, requester == null ? "" : requester);
                        });
                    }
        }, 500));

        startForeground(ID, createNotification());
        startScreenCapture(intent);

        Store.init(this);

        isRunning = true;

        return START_STICKY;
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Screen Capture", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Notification.Builder notificationBuilder = new Notification.Builder(this, CHANNEL_ID).setContentTitle("Screen Shared").setContentText("Click to stop screen sharing").setSmallIcon(R.drawable.eye).setContentIntent(stopIntent()).setOngoing(true);

        return notificationBuilder.build();
    }

    private PendingIntent stopIntent() {
        Intent stopIntent = new Intent(this, getClass());
        stopIntent.setAction("STOP_SERVICE");

        return PendingIntent.getService(this, 64, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void startScreenCapture(Intent intent) {
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            data = intent.getParcelableExtra("data", Intent.class);
        } else {
            data = intent.getParcelableExtra("data");
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);

            if (mediaProjection != null) {
                projectionCallback = new MediaProjection.Callback() {
                };
                mediaProjection.registerCallback(projectionCallback, new Handler(Looper.getMainLooper()));

                captureScreen(mediaProjection);
            } else {
                Log.e("media projection", "Failed to initialize MediaProjection");
            }
        } else {
            Log.e("media projection", "Invalid result code or data for MediaProjection");
        }
    }

    private void captureScreen(MediaProjection mediaProjection) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenDensity = metrics.densityDpi;
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 3);

        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(),
                new VirtualDisplay.Callback() {

        }, null);
    }

    private void processBitmap(Context context, Bitmap bitmap, String requester) {
        if (!requester.isEmpty()) {
            long now = System.currentTimeMillis();
            if (now - lastCaptureTime < 1000) {
                return;
            }
            lastCaptureTime = now;

            boolean handled = false;
            String appName = "Unknown";
            try {
                if(PermissionCheck.hasUsageAccessPermission(this)) {
                    UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                    long endTime = System.currentTimeMillis();
                    long beginTime = endTime - 1000 * 60;

                    List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);

                    if (usageStatsList != null && !usageStatsList.isEmpty()) {
                        UsageStats recentStat = null;
                        for (UsageStats stat : usageStatsList) {
                            if ((recentStat == null || stat.getLastTimeUsed() > recentStat.getLastTimeUsed())) {
                                if (!stat.getPackageName().equals("com.sdidsa.bondcheck"))
                                    recentStat = stat;
                            }
                        }

                        if (recentStat != null) {
                            String foregroundApp = recentStat.getPackageName();
                            try {
                                appName = getAppName(context, foregroundApp);
                            } catch (PackageManager.NameNotFoundException ex) {
                                appName = "No App";
                            }
                            if (Store.getCensoredApps().contains(foregroundApp)) {
                                handled = true;

                                String finalAppName = appName;
                                Platform.runBack(() -> {
                                    Bitmap blurred = ScreenshotUtils.gaussianBlur(bitmap, 50);
                                    Bitmap withLogo = ScreenshotUtils.addLogoToBlurredBitmap(context, blurred, foregroundApp);
                                    onCapture(withLogo, finalAppName, requester);
                                });
                            }
                        }
                    }
                }

                if (!handled) onCapture(bitmap, appName, requester);
            } catch (Exception e) {
                ErrorHandler.handle(e, "process screenshot");
            }
        }
    }

    private void onCapture(Bitmap c, String app, String requester) {
        Platform.runBack(() -> {
            DBLocation location = LocationUtils.getLocation(this);
            SaveItemRequest save = new SaveItemRequest(
                    requester, ImageProxy.saveTemp(c),
                    app, location);
            Call<GenericResponse> call = save.saveScreenshot(this);
            com.sdidsa.bondcheck.http.services.Service.enqueue(call, resp -> {
                if (resp.isSuccessful()) {
                    Log.i("screenshot", "saved for " + requester);
                } else {
                    Log.e("screenshot", "failed to save");
                }
            });
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void suicide() {
        unregisterReceiver(receiver);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        virtualDisplay = null;

        if (imageReader != null) {
            imageReader.close();
        }
        imageReader = null;

        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(projectionCallback);
            mediaProjection.stop();
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(ID);
    }
}