package com.jean.martin.android.app.customseekbar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * TODO: document your custom view class.
 */
public class Seekaxis extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;
    private int mPadding = 0; // padding relative to the edge.
    private int mCollapsedPadding = 0;
    private int mExpandedPadding = 50;

    private int mCursor = 0;

    private Boolean mExpanded = false; // whether the bar is activated by the user.


    private static final String TAG = "OPEN";

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private List<Dot> mData; // data should be sorted by dot.mPosition.

    class Dot implements Comparable<Dot> {
        int mColor;
        float mPosition; // percentage
        float mScale = 1; // should this dot be scaled to emphasize.

        public Dot(int color, int pos) {
            mColor = color;
            mPosition = pos;
        }

        @Override
        public int compareTo(Dot another) {
            if (mPosition < another.mPosition)
                return -1;
            else if (mPosition == another.mPosition)
                return 0;
            else return 1;
        }
    }

    public Seekaxis(Context context) {
        super(context);
        init(null, 0);
    }

    public Seekaxis(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Seekaxis(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Seekaxis, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.Seekaxis_exampleString);
        mExampleColor = a.getColor(
                R.styleable.Seekaxis_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.Seekaxis_exampleDimension,
                mExampleDimension);

        mExpanded = a.getBoolean(R.styleable.Seekaxis_expanded, mExpanded);
        mCursor = a.getInteger(R.styleable.Seekaxis_cursor, mCursor);


        if (a.hasValue(R.styleable.Seekaxis_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.Seekaxis_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        initData();
    }

    private void initData() {
        mData = new ArrayList();
        int length = new Random().nextInt(10) + 0;
        List<Integer> colorList = new ArrayList<>(Arrays.asList(
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA
        ));
        for (int i = 0; i < length; i++) {
            int color = colorList.get(new Random().nextInt(colorList.size()));
            int pos = new Random().nextInt(100);
            Dot dot = new Dot(color, pos);
            int r = new Random().nextInt(20);
            // random set dot scale, 5% with 3, 20% with 2
            if (r == 0) dot.mScale = 3;
            else if (r < 4) dot.mScale = 2;
            else dot.mScale = 1;
            mData.add(dot);
        }
        Collections.sort(mData);
        StringBuilder sb = new StringBuilder();
        for(Dot dot :mData){
            sb.append(dot.mPosition+", ");
        }
        Log.d(TAG, "initData: "+sb.toString());
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    public void setPaddingRight(int padding) {
        mPadding = padding;
        setPadding(getPaddingLeft(), getPaddingTop(), padding, getPaddingBottom());
    }

    public int getRightPadding() {
        return mPadding;
    }

    public void setExpanded(Boolean expanded) {
        mExpanded = expanded;
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();

        int[] values;
        if (expanded) values = new int[]{mExpandedPadding, mCollapsedPadding};
        else values = new int[]{mCollapsedPadding, mExpandedPadding};
        ValueAnimator animator = ValueAnimator.ofInt(values);
        animator.setDuration(500);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.rightMargin = (Integer) animation.getAnimatedValue();
                requestLayout();
            }
        });
        animator.start();
    }

    public Boolean getExpanded() {
        return mExpanded;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        Log.d(TAG, "onTouchEvent: "+event.toString());

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float y = event.getY() / getHeight();
                mCursor = getCursorByPosition(y);
                Log.d(TAG, "onTouchEvent: mCursor="+mCursor +" ");
                invalidate();
                break;
        }
        //return super.onTouchEvent(event);
        return true;
    }

    private int getCursorByPosition(float y) {
        return getCursorByPosition(y, mData, 0, mData.size());
    }

    private static int getCursorByPosition(float y, List<Dot> list, int start, int end) {
        int cursor = 0;
        if (list == null || list.size() < 2) {
            cursor = 0;
            return cursor;
        }
        if (end - start < 1)
            return end;
        int mid = (end - start) / 2 + start;
        if (y < list.get(mid).mPosition)
            cursor = getCursorByPosition(y, list, start, mid);
        else if (y > list.get(mid).mPosition)
            cursor = getCursorByPosition(y, list, mid, end);
        else if (y == list.get(mid).mPosition)
            cursor = mid;

        return cursor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;


        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5);
        canvas.drawLine(10.0f, 0.0f, 10.0f, getHeight(), linePaint);

        if (!mExpanded) {
            linePaint.setColor(Color.BLACK);
            float pos = 0;
            if (mData != null && mData.size() > mCursor)
                pos = getHeight() * mData.get(mCursor).mPosition;
            canvas.drawLine(10.0f, 0.0f, 10.0f, pos, linePaint);
        } else {
            // Draw the text.
//        canvas.drawText(mExampleString,
//                paddingLeft + (contentWidth - mTextWidth) / 2,
//                paddingTop + (contentHeight + mTextHeight) / 2,
//                mTextPaint);

            Paint pointPaint = new Paint();
            pointPaint.setColor(Color.RED);
            for (Dot item : mData) {
                float top = (float) (item.mPosition / 100.0f * getHeight());
                pointPaint.setColor(item.mColor);
                pointPaint.setStyle(Paint.Style.FILL);
                float radius = (float) (5 * item.mScale);
                canvas.drawCircle(10.0f, top, radius, pointPaint);
            }
        }


        // draw cursor
        drawCursor(canvas);
    }

    private float getCursorPos() {
        float cursorPos = 0f;
        if (mData != null && mData.size() > mCursor)
            cursorPos = mData.get(mCursor).mPosition;
        return cursorPos;
    }

    private void drawCursor(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        Point origin = new Point(10, (int) (getHeight() * getCursorPos()));
        Log.d(TAG, "drawCursor: origin="+origin.toString()+ " height="+getHeight() +" cursor="+mCursor);

        Point point1 = origin;
        Point point2 = new Point(origin.x + 20, origin.y - 10);
        Point point3 = new Point(origin.x + 20, origin.y + 10);

        Path cursorTriangle = new Path();
        cursorTriangle.moveTo(point1.x, point1.y);
        cursorTriangle.lineTo(point2.x, point2.y);
        cursorTriangle.moveTo(point2.x, point2.y);
        cursorTriangle.lineTo(point3.x, point3.y);
        cursorTriangle.moveTo(point3.x, point3.y);
        cursorTriangle.lineTo(point1.x, point1.y);
        cursorTriangle.close();
        canvas.drawPath(cursorTriangle, paint);
    }


    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
