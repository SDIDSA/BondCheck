package com.sdidsa.bondcheck.abs.components.controls.image;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ScaleGestureDetectorCompat;

import com.sdidsa.bondcheck.abs.data.observable.ChangeListener;

/**
 * ZoomageView is a pinch-to-zoom extension of {@link ImageView}, providing a smooth
 * user experience and a very natural feel when zooming and translating. It also supports
 * automatic resetting, and allows for exterior bounds restriction to keep the image within
 * visible window.
 */
public class ZoomageView extends AppCompatImageView implements OnScaleGestureListener {

    private static final float MIN_SCALE = 1f;
    private static final float MAX_SCALE = 8f;
    private final int RESET_DURATION = 200;

    private ScaleType startScaleType;

    // These matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix startMatrix = new Matrix();

    private float[] matrixValues = new float[9];
    private float[] startValues = null;

    private float minScale = MIN_SCALE;
    private float maxScale = MAX_SCALE;

    //the adjusted scale bounds that account for an image's starting scale values
    private float calculatedMinScale = MIN_SCALE;
    private float calculatedMaxScale = MAX_SCALE;

    private final RectF bounds = new RectF();

    private boolean translatable;
    private boolean zoomable;
    private boolean doubleTapToZoom;
    private boolean restrictBounds;
    private boolean animateOnReset;
    private boolean autoCenter;
    private float doubleTapToZoomScaleFactor;
    @AutoResetMode
    private int autoResetMode;

    private PointF last = new PointF(0, 0);
    private float startScale = 1f;
    private float scaleBy = 1f;
    private float currentScaleFactor = 1f;
    private int previousPointerCount = 1;
    private int currentPointerCount = 0;

    private ScaleGestureDetector scaleDetector;
    private ValueAnimator resetAnimator;

    private GestureDetector gestureDetector;
    private boolean doubleTapDetected = false;
    private boolean singleTapDetected = false;

    private ChangeListener<Float> onScale;
    private Runnable onSingleTap;

    public ZoomageView(Context context) {
        super(context);
        init(context, null);
    }

    public ZoomageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ZoomageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        scaleDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, gestureListener);
        ScaleGestureDetectorCompat.setQuickScaleEnabled(scaleDetector, false);
        startScaleType = getScaleType();

        zoomable = true;
        translatable = true;
        animateOnReset = true;
        autoCenter = true;
        restrictBounds = false;
        doubleTapToZoom = true;
        minScale = MIN_SCALE;
        maxScale = MAX_SCALE;
        doubleTapToZoomScaleFactor = 3;
        autoResetMode = AutoResetMode.UNDER;

        verifyScaleRange();
    }

    public boolean isReady() {
        return startValues != null;
    }

    public void setOnScale(ChangeListener<Float> onScale) {
        this.onScale = onScale;
    }

    private float oldScale = -1;
    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        float[] mat = new float[9];
        matrix.getValues(mat);
        float newScale = mat[Matrix.MSCALE_X] /
                startValues[Matrix.MSCALE_X];
        if(onScale != null && oldScale != -1 && oldScale != newScale) {
            onScale.changed(oldScale, newScale);
        }
        oldScale = newScale;
    }

    private void verifyScaleRange() {
        if (minScale >= maxScale) {
            throw new IllegalStateException("minScale must be less than maxScale");
        }

        if (minScale < 0) {
            throw new IllegalStateException("minScale must be greater than 0");
        }

        if (maxScale < 0) {
            throw new IllegalStateException("maxScale must be greater than 0");
        }

        if (doubleTapToZoomScaleFactor > maxScale) {
            doubleTapToZoomScaleFactor = maxScale;
        }

        if (doubleTapToZoomScaleFactor < minScale) {
            doubleTapToZoomScaleFactor = minScale;
        }
    }

    /**
     * Set the minimum and maximum allowed scale for zooming. {@code minScale} cannot
     * be greater than {@code maxScale} and neither can be 0 or less. This will result
     * in an {@link IllegalStateException}.
     *
     * @param minScale minimum allowed scale
     * @param maxScale maximum allowed scale
     */
    public void setScaleRange(final float minScale, final float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;

        startValues = null;

        verifyScaleRange();
    }

    /**
     * Returns whether the image is translatable.
     *
     * @return true if translation of image is allowed, false otherwise
     */
    public boolean isTranslatable() {
        return translatable;
    }

    /**
     * Set the image's translatable state.
     *
     * @param translatable true to enable translation, false to disable it
     */
    public void setTranslatable(boolean translatable) {
        this.translatable = translatable;
    }

    /**
     * Returns the zoomable state of the image.
     *
     * @return true if pinch-zooming of the image is allowed, false otherwise.
     */
    public boolean isZoomable() {
        return zoomable;
    }

    /**
     * Set the zoomable state of the image.
     *
     * @param zoomable true to enable pinch-zooming of the image, false to disable it
     */
    public void setZoomable(final boolean zoomable) {
        this.zoomable = zoomable;
    }

    /**
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     *
     * @return true if image bounds are restricted to the view's edges, false otherwise
     */
    public boolean getRestrictBounds() {
        return restrictBounds;
    }

    /**
     * Set the restrictBounds status of the image.
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     *
     * @param restrictBounds true if image bounds should be restricted to the view's edges, false otherwise
     */
    public void setRestrictBounds(final boolean restrictBounds) {
        this.restrictBounds = restrictBounds;
    }

    /**
     * Returns status of animateOnReset. This causes the image to smoothly animate back
     * to its start position when reset. Default value is true.
     *
     * @return true if animateOnReset is enabled, false otherwise
     */
    public boolean getAnimateOnReset() {
        return animateOnReset;
    }

    /**
     * Set whether or not the image should animate when resetting.
     *
     * @param animateOnReset true if image should animate when resetting, false to snap
     */
    public void setAnimateOnReset(final boolean animateOnReset) {
        this.animateOnReset = animateOnReset;
    }

    /**
     * Get the current {@link AutoResetMode} mode of the image. Default value is {@link AutoResetMode#UNDER}.
     *
     * @return the current {@link AutoResetMode} mode, one of {@link AutoResetMode#OVER OVER}, {@link AutoResetMode#UNDER UNDER},
     * {@link AutoResetMode#ALWAYS ALWAYS}, or {@link AutoResetMode#NEVER NEVER}
     */
    @AutoResetMode
    public int getAutoResetMode() {
        return autoResetMode;
    }

    /**
     * Set the {@link AutoResetMode} mode for the image.
     *
     * @param autoReset the desired mode, one of {@link AutoResetMode#OVER OVER}, {@link AutoResetMode#UNDER UNDER},
     *                  {@link AutoResetMode#ALWAYS ALWAYS}, or {@link AutoResetMode#NEVER NEVER}
     */
    public void setAutoResetMode(@AutoResetMode final int autoReset) {
        this.autoResetMode = autoReset;
    }

    /**
     * Whether or not the image should automatically center itself when it's dragged partially or
     * fully out of view.
     *
     * @return true if image should center itself automatically, false if it should not
     */
    public boolean getAutoCenter() {
        return autoCenter;
    }

    /**
     * Set whether or not the image should automatically center itself when it's dragged
     * partially or fully out of view.
     *
     * @param autoCenter true if image should center itself automatically, false if it should not
     */
    public void setAutoCenter(final boolean autoCenter) {
        this.autoCenter = autoCenter;
    }

    /**
     * Gets double tap to zoom state.
     *
     * @return whether double tap to zoom is enabled
     */
    public boolean getDoubleTapToZoom() {
        return doubleTapToZoom;
    }

    /**
     * Sets double tap to zoom state.
     *
     * @param doubleTapToZoom true if double tap to zoom should be enabled
     */
    public void setDoubleTapToZoom(boolean doubleTapToZoom) {
        this.doubleTapToZoom = doubleTapToZoom;
    }

    /**
     * Gets the double tap to zoom scale factor.
     *
     * @return double tap to zoom scale factor
     */
    public float getDoubleTapToZoomScaleFactor() {
        return doubleTapToZoomScaleFactor;
    }

    /**
     * Sets the double tap to zoom scale factor. Can be a maximum of max scale.
     *
     * @param doubleTapToZoomScaleFactor the scale factor you want to zoom to when double tap occurs
     */
    public void setDoubleTapToZoomScaleFactor(float doubleTapToZoomScaleFactor) {
        this.doubleTapToZoomScaleFactor = doubleTapToZoomScaleFactor;
        verifyScaleRange();
    }

    /**
     * Get the current scale factor of the image, in relation to its starting size.
     *
     * @return the current scale factor
     */
    public float getCurrentScaleFactor() {
        return currentScaleFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScaleType(@Nullable ScaleType scaleType) {
        if (scaleType != null) {
            super.setScaleType(scaleType);
            startScaleType = scaleType;
            startValues = null;
        }
    }

    /**
     * Set enabled state of the view. Note that this will reset the image's
     * {@link android.widget.ImageView.ScaleType} to its pre-zoom state.
     *
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            setScaleType(startScaleType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setScaleType(startScaleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        setScaleType(startScaleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setScaleType(startScaleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        setScaleType(startScaleType);
    }

    /**
     * Update the bounds of the displayed image based on the current matrix.
     *
     * @param values the image's current matrix values.
     */
    private void updateBounds(final float[] values) {
        if (getDrawable() != null) {
            bounds.set(values[Matrix.MTRANS_X],
                    values[Matrix.MTRANS_Y],
                    getDrawable().getIntrinsicWidth() * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X],
                    getDrawable().getIntrinsicHeight() * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y]);
        }
    }

    /**
     * Get the width of the displayed image.
     *
     * @return the current width of the image as displayed (not the width of the {@link ImageView} itself.
     */
    private float getCurrentDisplayedWidth() {
        if (getDrawable() != null)
            return getDrawable().getIntrinsicWidth() * matrixValues[Matrix.MSCALE_X];
        else
            return 0;
    }

    /**
     * Get the height of the displayed image.
     *
     * @return the current height of the image as displayed (not the height of the {@link ImageView} itself.
     */
    private float getCurrentDisplayedHeight() {
        if (getDrawable() != null)
            return getDrawable().getIntrinsicHeight() * matrixValues[Matrix.MSCALE_Y];
        else
            return 0;
    }

    /**
     * Remember our starting values so we can animate our image back to its original position.
     */
    private void setStartValues() {
        startValues = new float[9];
        startMatrix = new Matrix(getImageMatrix());
        startMatrix.getValues(startValues);
        calculatedMinScale = minScale * startValues[Matrix.MSCALE_X];
        calculatedMaxScale = maxScale * startValues[Matrix.MSCALE_X];
    }

    public void setOnSingleTap(Runnable onSingleTap) {
        this.onSingleTap = onSingleTap;
    }

    private int initX, initY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if(event.getAction() == MotionEvent.ACTION_DOWN
                && event.getPointerCount() == 1 && !doubleTapDetected) {
            initX = (int) event.getX();
            initY = (int) event.getY();
        }

        if(event.getAction() == MotionEvent.ACTION_UP
                && event.getPointerCount() == 1
                && !doubleTapDetected
                && (int) event.getX() == initX
                && (int) event.getY() == initY) {
            if(onSingleTap != null) {
                onSingleTap.run();
            }
            return true;
        }

        currentPointerCount = event.getPointerCount();

        if (!isClickable() && isEnabled() && (zoomable || translatable)) {
            if (getScaleType() != ScaleType.MATRIX) {
                super.setScaleType(ScaleType.MATRIX);
            }

            if (startValues == null) {
                setStartValues();
            }

            //get the current state of the image matrix, its values, and the bounds of the drawn bitmap
            matrix.set(getImageMatrix());
            matrix.getValues(matrixValues);
            updateBounds(matrixValues);

            scaleDetector.onTouchEvent(event);

            if (doubleTapToZoom && doubleTapDetected) {
                doubleTapDetected = false;
                singleTapDetected = false;
                if (matrixValues[Matrix.MSCALE_X] != startValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    Matrix zoomMatrix = new Matrix(matrix);
                    zoomMatrix.postScale(doubleTapToZoomScaleFactor, doubleTapToZoomScaleFactor, scaleDetector.getFocusX(), scaleDetector.getFocusY());
                    animateScaleAndTranslationToMatrix(zoomMatrix, RESET_DURATION);
                }
                return true;
            } else if (!singleTapDetected) {
                /* if the event is a down touch, or if the number of touch points changed,
                 * we should reset our start point, as event origins have likely shifted to a
                 * different part of the screen*/
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                        currentPointerCount != previousPointerCount) {
                    last.set(scaleDetector.getFocusX(), scaleDetector.getFocusY());
                } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                    final float focusx = scaleDetector.getFocusX();
                    final float focusy = scaleDetector.getFocusY();

                    if (allowTranslate(event)) {
                        //calculate the distance for translation
                        float xdistance = getXDistance(focusx, last.x);
                        float ydistance = getYDistance(focusy, last.y);
                        matrix.postTranslate(xdistance, ydistance);
                    }

                    if (allowZoom(event)) {
                        matrix.postScale(scaleBy, scaleBy, focusx, focusy);
                        currentScaleFactor = matrixValues[Matrix.MSCALE_X] / startValues[Matrix.MSCALE_X];
                    }

                    setImageMatrix(matrix);

                    last.set(focusx, focusy);
                }

                if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                        event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    scaleBy = 1f;
                    resetImage();
                }
            }

            getParent().requestDisallowInterceptTouchEvent(disallowParentTouch(event));

            //this tracks whether they have changed the number of fingers down


            previousPointerCount = currentPointerCount;
            return true;
        }
        previousPointerCount = currentPointerCount;

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    protected boolean disallowParentTouch(MotionEvent event) {
        return currentPointerCount > 1 || currentScaleFactor > 1.0f || isAnimating();
    }

    protected boolean allowTranslate(MotionEvent event) {
        return translatable && currentScaleFactor > 1.0f;
    }

    protected boolean allowZoom(MotionEvent event) {
        return zoomable;
    }

    private boolean isAnimating() {
        return resetAnimator != null && resetAnimator.isRunning();
    }

    /**
     * Reset the image based on the specified {@link AutoResetMode} mode.
     */
    private void resetImage() {
        switch (autoResetMode) {
            case AutoResetMode.UNDER:
                if (matrixValues[Matrix.MSCALE_X] <= startValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    center();
                }
                break;
            case AutoResetMode.OVER:
                if (matrixValues[Matrix.MSCALE_X] >= startValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    center();
                }
                break;
            case AutoResetMode.ALWAYS:
                reset();
                break;
            case AutoResetMode.NEVER:
                center();
        }
    }

    /**
     * This helps to keep the image on-screen by animating the translation to the nearest
     * edge, both vertically and horizontally.
     */
    private void center() {
        if (autoCenter) {
            animateTranslationX();
            animateTranslationY();
        }
    }

    /**
     * Reset image back to its original size. Will snap back to original size
     * if animation on reset is disabled via {@link #setAnimateOnReset(boolean)}.
     */
    public void reset() {
        reset(animateOnReset);
    }

    /**
     * Reset image back to its starting size. If {@code animate} is false, image
     * will snap back to its original size.
     *
     * @param animate animate the image back to its starting size
     */
    public void reset(final boolean animate) {
        if (animate) {
            animateToStartMatrix();
        } else {
            setImageMatrix(startMatrix);
        }
    }

    /**
     * Animate the matrix back to its original position after the user stopped interacting with it.
     */
    private void animateToStartMatrix() {
        animateScaleAndTranslationToMatrix(startMatrix, RESET_DURATION);
    }

    /**
     * Animate the scale and translation of the current matrix to the target
     * matrix.
     *
     * @param targetMatrix the target matrix to animate values to
     */
    private void animateScaleAndTranslationToMatrix(final Matrix targetMatrix, final int duration) {

        final float[] targetValues = new float[9];
        targetMatrix.getValues(targetValues);

        final Matrix beginMatrix = new Matrix(getImageMatrix());
        beginMatrix.getValues(matrixValues);

        //difference in current and original values
        final float xsdiff = targetValues[Matrix.MSCALE_X] - matrixValues[Matrix.MSCALE_X];
        final float ysdiff = targetValues[Matrix.MSCALE_Y] - matrixValues[Matrix.MSCALE_Y];
        final float xtdiff = targetValues[Matrix.MTRANS_X] - matrixValues[Matrix.MTRANS_X];
        final float ytdiff = targetValues[Matrix.MTRANS_Y] - matrixValues[Matrix.MTRANS_Y];

        resetAnimator = ValueAnimator.ofFloat(0, 1f);
        resetAnimator.addUpdateListener(new AnimatorUpdateListener() {

            final Matrix activeMatrix = new Matrix(getImageMatrix());
            final float[] values = new float[9];

            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float val = (Float) animation.getAnimatedValue();
                activeMatrix.set(beginMatrix);
                activeMatrix.getValues(values);
                values[Matrix.MTRANS_X] = values[Matrix.MTRANS_X] + xtdiff * val;
                values[Matrix.MTRANS_Y] = values[Matrix.MTRANS_Y] + ytdiff * val;
                values[Matrix.MSCALE_X] = values[Matrix.MSCALE_X] + xsdiff * val;
                values[Matrix.MSCALE_Y] = values[Matrix.MSCALE_Y] + ysdiff * val;
                activeMatrix.setValues(values);
                setImageMatrix(activeMatrix);
            }
        });

        resetAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setImageMatrix(targetMatrix);
            }
        });

        resetAnimator.setDuration(duration);
        resetAnimator.start();
    }

    private void animateTranslationX() {
        if (getCurrentDisplayedWidth() > getWidth()) {
            //the left edge is too far to the interior
            if (bounds.left > 0) {
                animateMatrixIndex(Matrix.MTRANS_X, 0);
            }
            //the right edge is too far to the interior
            else if (bounds.right < getWidth()) {
                animateMatrixIndex(Matrix.MTRANS_X, bounds.left + getWidth() - bounds.right);
            }
        } else {
            //left edge needs to be pulled in, and should be considered before the right edge
            if (bounds.left < 0) {
                animateMatrixIndex(Matrix.MTRANS_X, 0);
            }
            //right edge needs to be pulled in
            else if (bounds.right > getWidth()) {
                animateMatrixIndex(Matrix.MTRANS_X, bounds.left + getWidth() - bounds.right);
            }
        }
    }

    private void animateTranslationY() {
        if (getCurrentDisplayedHeight() > getHeight()) {
            //the top edge is too far to the interior
            if (bounds.top > 0) {
                animateMatrixIndex(Matrix.MTRANS_Y, 0);
            }
            //the bottom edge is too far to the interior
            else if (bounds.bottom < getHeight()) {
                animateMatrixIndex(Matrix.MTRANS_Y, bounds.top + getHeight() - bounds.bottom);
            }
        } else {
            //top needs to be pulled in, and needs to be considered before the bottom edge
            if (bounds.top < 0) {
                animateMatrixIndex(Matrix.MTRANS_Y, 0);
            }
            //bottom edge needs to be pulled in
            else if (bounds.bottom > getHeight()) {
                animateMatrixIndex(Matrix.MTRANS_Y, bounds.top + getHeight() - bounds.bottom);
            }
        }
    }

    private void animateMatrixIndex(final int index, final float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(matrixValues[index], to);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            final float[] values = new float[9];
            Matrix current = new Matrix();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                current.set(getImageMatrix());
                current.getValues(values);
                values[index] = (Float) animation.getAnimatedValue();
                current.setValues(values);
                setImageMatrix(current);
            }
        });
        animator.setDuration(RESET_DURATION);
        animator.start();
    }

    /**
     * Get the x distance to translate the current image.
     *
     * @param toX   the current x location of touch focus
     * @param fromX the last x location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private float getXDistance(final float toX, final float fromX) {
        float xdistance = toX - fromX;

        if (restrictBounds) {
            xdistance = getRestrictedXDistance(xdistance);
        }

        //prevents image from translating an infinite distance offscreen
        if (bounds.right + xdistance < 0) {
            xdistance = -bounds.right;
        } else if (bounds.left + xdistance > getWidth()) {
            xdistance = getWidth() - bounds.left;
        }

        return xdistance;
    }

    /**
     * Get the horizontal distance to translate the current image, but restrict
     * it to the outer bounds of the {@link ImageView}. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     *
     * @param xdistance the current desired horizontal distance to translate
     * @return the actual horizontal distance to translate with bounds restrictions
     */
    private float getRestrictedXDistance(final float xdistance) {
        float restrictedXDistance = xdistance;

        if (getCurrentDisplayedWidth() >= getWidth()) {
            if (bounds.left <= 0 && bounds.left + xdistance > 0 && !scaleDetector.isInProgress()) {
                restrictedXDistance = -bounds.left;
            } else if (bounds.right >= getWidth() && bounds.right + xdistance < getWidth() && !scaleDetector.isInProgress()) {
                restrictedXDistance = getWidth() - bounds.right;
            }
        } else if (!scaleDetector.isInProgress()) {
            if (bounds.left >= 0 && bounds.left + xdistance < 0) {
                restrictedXDistance = -bounds.left;
            } else if (bounds.right <= getWidth() && bounds.right + xdistance > getWidth()) {
                restrictedXDistance = getWidth() - bounds.right;
            }
        }

        return restrictedXDistance;
    }

    /**
     * Get the y distance to translate the current image.
     *
     * @param toY   the current y location of touch focus
     * @param fromY the last y location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private float getYDistance(final float toY, final float fromY) {
        float ydistance = toY - fromY;

        if (restrictBounds) {
            ydistance = getRestrictedYDistance(ydistance);
        }

        //prevents image from translating an infinite distance offscreen
        if (bounds.bottom + ydistance < 0) {
            ydistance = -bounds.bottom;
        } else if (bounds.top + ydistance > getHeight()) {
            ydistance = getHeight() - bounds.top;
        }

        return ydistance;
    }

    /**
     * Get the vertical distance to translate the current image, but restrict
     * it to the outer bounds of the {@link ImageView}. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     *
     * @param ydistance the current desired vertical distance to translate
     * @return the actual vertical distance to translate with bounds restrictions
     */
    private float getRestrictedYDistance(final float ydistance) {
        float restrictedYDistance = ydistance;

        if (getCurrentDisplayedHeight() >= getHeight()) {
            if (bounds.top <= 0 && bounds.top + ydistance > 0 && !scaleDetector.isInProgress()) {
                restrictedYDistance = -bounds.top;
            } else if (bounds.bottom >= getHeight() && bounds.bottom + ydistance < getHeight() && !scaleDetector.isInProgress()) {
                restrictedYDistance = getHeight() - bounds.bottom;
            }
        } else if (!scaleDetector.isInProgress()) {
            if (bounds.top >= 0 && bounds.top + ydistance < 0) {
                restrictedYDistance = -bounds.top;
            } else if (bounds.bottom <= getHeight() && bounds.bottom + ydistance > getHeight()) {
                restrictedYDistance = getHeight() - bounds.bottom;
            }
        }

        return restrictedYDistance;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        //calculate value we should scale by, ultimately the scale will be startScale*scaleFactor
        scaleBy = (startScale * detector.getScaleFactor()) / matrixValues[Matrix.MSCALE_X];

        //what the scaling should end up at after the transformation
        final float projectedScale = scaleBy * matrixValues[Matrix.MSCALE_X];

        //clamp to the min/max if it's going over
        if (projectedScale < calculatedMinScale) {
            scaleBy = calculatedMinScale / matrixValues[Matrix.MSCALE_X];
        } else if (projectedScale > calculatedMaxScale) {
            scaleBy = calculatedMaxScale / matrixValues[Matrix.MSCALE_X];
        }

        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        startScale = matrixValues[Matrix.MSCALE_X];
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        scaleBy = 1f;
    }

    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                doubleTapDetected = true;
            }

            singleTapDetected = false;

            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            singleTapDetected = true;
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            singleTapDetected = false;
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    };

    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
