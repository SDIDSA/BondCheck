package com.sdidsa.bondcheck.app.services;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class FloatingService extends Service {
    private WindowManager mWindowManager;
    private StackPane root;
    private WindowManager.LayoutParams params;
    private int screenWidth;
    private int margin;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme_BondCheck_Dark);
        margin = ContextUtils.dipToPx(10, this);
        // Create the floating view layout
        root = new ColoredStackPane(this, Style.ACCENT);
        root.setPadding(12);
        root.setCornerRadius(100);
        root.setAlpha(.7f);

        ColoredIcon icon = new ColoredIcon(this, Style.WHITE,
                R.drawable.heart, 26);

        root.addCentered(icon);

        // Set up the window manager
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = margin;
        params.y = 100;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(root, params);

        screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Set up touch listener for dragging the view
        root.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(root, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        snapToSide();
                        return true;
                }
                return false;
            }
        });
    }

    private void snapToSide() {
        int middle = screenWidth / 2;
        float nearestXWall = params.x + root.getWidth() / 2 >= middle ?
                screenWidth - root.getWidth() - margin :
                margin;

        ValueAnimator animator = ValueAnimator.ofInt(params.x, (int)nearestXWall);
        animator.setInterpolator(Interpolator.OVERSHOOT);
        animator.setDuration(400);
        animator.addUpdateListener(animation -> {
            params.x = (int) animation.getAnimatedValue();
            mWindowManager.updateViewLayout(root, params);
        });
        animator.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (root != null) mWindowManager.removeView(root);
    }
}