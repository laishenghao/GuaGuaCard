package com.haoye.guaguacard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Haoye on 2016/2/2.
 * Copyright © 2016 Haoye All Rights Reserved
 */
public class GuaGuaCard extends View {
    /**
     * 绘制线条的Paint,即用户手指绘制Path
     */
    private Paint mHandPaint = new Paint();
    private Paint mBackPint  = new Paint();
    /**
     * 记录用户绘制的Path
     */
    private Path mPath = new Path();
    /**
     * 内存中创建的Canvas
     */
    private Canvas mCanvas;
    /**
     * mCanvas绘制内容在其上
     */
    private Bitmap mBitmap;
    //private Bitmap mBackBitmap;


    private Rect mTextBound = new Rect();
    private String mText = "600,0000,000";

    private int mLastX;
    private int mLastY;

    private boolean isComplete = false;

    /**
    * 统计擦除区域任务
    */
    private Runnable mRunnable = new Runnable() {
        private int[] mPixels;

        @Override
        public void run() {
            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            float wipeArea = 0;
            float totalArea = w * h;
            Bitmap bitmap = mBitmap;

            mPixels = new int[w * h];

            /**
             * 拿到所有的像素信息
             */
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            /**
             * 遍历统计擦除的区域
             */
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }

            /**
             * 根据所占百分比，进行一些操作
             */
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int)(wipeArea * 100 / totalArea);
                //Log.e("TAG", percent + "");
                if (percent > 70) {
                    isComplete = true;
                    postInvalidate();
                }
            }
        }

    };

    public GuaGuaCard(Context context) {
        this(context, null);
    }

    public GuaGuaCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuaGuaCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //context.getTheme().obtainStyledAttributes(attrs, R.styleable.ActionBar, defStyle, 0);
        init();
    }

    private void init() {
        mPath = new Path();
        // 设置画笔
        mHandPaint.setColor(Color.BLUE);
        mHandPaint.setAntiAlias(true);//
        mHandPaint.setDither(false);
        mHandPaint.setStyle(Paint.Style.STROKE);
        mHandPaint.setStrokeJoin(Paint.Join.ROUND); // 连接处圆角
        mHandPaint.setStrokeCap(Paint.Cap.ROUND); // 起始处圆角
        // 设置画笔宽度
        mHandPaint.setStrokeWidth(50);

        setUpOutPaint();

//        mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // 初始化bitmap
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

    }

    private void setUpOutPaint() {
        mBackPint.setStyle(Paint.Style.FILL);
        mBackPint.setTextScaleX(2f);
        mBackPint.setColor(Color.DKGRAY);
        mBackPint.setTextSize(32);
        mBackPint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBackground(canvas);

        if (!isComplete) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mCanvas.drawColor(Color.parseColor("#c0c0c0"));
            drawPath();
        }

    }

    private void drawBackground(Canvas canvas) {
        //绘制奖项
        canvas.drawColor(Color.parseColor("#666666"));
        //mBackPint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
                getHeight() / 2 + mTextBound.height() / 2, mBackPint);
    }

    private void drawPath() {
        mHandPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mHandPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mLastX = x;
            mLastY = y;
            mPath.moveTo(mLastX, mLastY);
            break;
        case MotionEvent.ACTION_MOVE:
            int dx = Math.abs(x - mLastX);
            int dy = Math.abs(y - mLastY);
            if (dx > 3 || dy > 3) {
                mPath.lineTo(x, y);
            }
            mLastX = x;
            mLastY = y;
            break;
        case MotionEvent.ACTION_UP:
            new Thread(mRunnable).start();
            break;
        }

        invalidate();
        return true;
    }
}
