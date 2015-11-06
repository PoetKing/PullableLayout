package com.poet.pullablelayout.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import com.poet.pullablelayout.Indicator;
import com.poet.pullablelayout.PullableLayout;
import com.poet.pullablelayout.State;

/**
 * 
 * @author poet
 * 2015-10-23下午4:54:44
 */
public class CircularArrow extends View implements Indicator {

    private Runnable mRunningRunnable = new Runnable() {
        @Override
        public void run() {
            mRunningDegree += 15;
            invalidate();
            postDelayed(this, 20);
        }
    };

    private int mRadius = (int) (getResources().getDisplayMetrics().density * 20);
    private int mArrowLen = mRadius / 5;
    private Paint mBgPaint, mCirclePaint, mArrowPaint;
    private RectF mCircleRect;
    private Path mCirclePath, mArrowPath;

    private float mScale;
    private float mRunningDegree;

    public CircularArrow(Context context) {
        super(context);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setShadowLayer(2, 0, 1, Color.argb(0x33, 0x00, 0x00, 0x00));
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Style.STROKE);
        mCirclePaint.setStrokeWidth(3);
        mCirclePaint.setColor(Color.GREEN);
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStyle(Style.FILL);
        mArrowPaint.setColor(Color.GREEN);
        mCircleRect = new RectF(mRadius / 2, mRadius / 2, mRadius / 2 * 3, mRadius / 2 * 3);
        mCirclePath = new Path();
        mArrowPath = new Path();
        MarginLayoutParams params = new MarginLayoutParams(-2, -2);
        params.topMargin = mRadius;
        this.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mRadius * 2, mRadius * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mRadius, mRadius, mRadius - 2, mBgPaint);
        if (mRunningDegree > 0) {
            canvas.rotate(mRunningDegree, mRadius, mRadius);
        } else {
            canvas.rotate(mScale * 180 - 90, mRadius, mRadius);
        }
        canvas.drawPath(mCirclePath, mCirclePaint);
        canvas.drawPath(mArrowPath, mArrowPaint);
    }

    @Override
    public void onAttach(PullableLayout layout) {
    }

    @Override
    public IndicatorSet getIndicatorSet() {
        return IndicatorSet.Above;
    }

    @Override
    public View getView(Context context) {
        return this;
    }

    @Override
    public void onStateChanged(State state) {
        mRunningDegree = 0;
        removeCallbacks(mRunningRunnable);
        if (state == State.RUNNING) {
            if (mScale < 1) {
                onPull(1);
            }
            post(mRunningRunnable);
        }
    }

    @Override
    public void onPull(float scale) {
        mScale = scale;
        if (scale > 1) {
            scale = 1;
        }
        this.setAlpha(scale * scale);
        mCirclePath.reset();
        mArrowPath.reset();
        float angle = 300 * scale;
        mCirclePath.addArc(mCircleRect, 0, angle);
        float x = (float) (Math.cos(Math.toRadians(angle)) * mRadius / 2) + mRadius;
        float y = (float) (Math.sin(Math.toRadians(angle)) * mRadius / 2) + mRadius;
        int arrowLen = (int) (mArrowLen * scale);
        float x1 = (float) (x + Math.cos(Math.toRadians(180 - angle)) * arrowLen);
        float y1 = (float) (y - Math.sin(Math.toRadians(180 - angle)) * arrowLen);
        float x2 = x * 2 - x1;
        float y2 = y * 2 - y1;
        float dAngle = (float) (180 * arrowLen / (mRadius / 2) / Math.PI);
        float dRadius = arrowLen * arrowLen / (mRadius / 2) / 2;
        float x3 = (float) (Math.cos(Math.toRadians(angle + dAngle)) * (mRadius / 2 + dRadius)) + mRadius;
        float y3 = (float) (Math.sin(Math.toRadians(angle + dAngle)) * (mRadius / 2 + dRadius)) + mRadius;
        mArrowPath.moveTo(x1, y1);
        mArrowPath.lineTo(x2, y2);
        mArrowPath.lineTo(x3, y3);
        invalidate();
    }

    @Override
    public float maxPullPercent() {
        return 2f;
    }

    public void setBgColor(int color) {
        mBgPaint.setColor(color);
    }

    public void setBgShadowColor(int color) {
        mBgPaint.setShadowLayer(2, 0, 1, color);
    }

    public void setCircularArrowColor(int color) {
        mArrowPaint.setColor(color);
        mCirclePaint.setColor(color);
    }
}
