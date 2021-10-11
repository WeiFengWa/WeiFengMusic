package com.wei.music.view;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import java.util.List;
import java.util.ArrayList;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.media.audiofx.Equalizer;
import android.media.audiofx.BassBoost;
import android.widget.Toast;
import android.graphics.RectF;
import android.graphics.DashPathEffect;
import android.graphics.PathDashPathEffect;
import android.graphics.CornerPathEffect;

public class EqualizerView extends View {

    private int width, hight;
    private int margin;
    
    private RectF mEqRectf;
    private RectF mBaRectf;
    private RectF mBarRect = new RectF();
    private Path mBarPath = new Path();

    private Paint mPaint;
    private Paint mPathPaint;
    private Paint mViewPaint;

    private Path mPath;

    private List<PointF> mPoints = new ArrayList<>();
    //滑块

    private int index;
    private int bands = 0;
    private short minEqLevel, maxEqLevel;
    //正在滑的
    private int[] mEqValue;
    //数据
    private int[] khzs; 

    private List<PointF> fList = new ArrayList<>();
    //控制点

    private Equalizer mEqualizer;
    
    private BassBoost mBassBoost;
    private float mBaValue = 500;
    
    private boolean isEQ = true;
    private boolean isMove = false;

    private CornerPathEffect pathEffect;
    
    public EqualizerView(Context context) {
        super(context);
        init();
    }

    public EqualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(8);
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(dip2px(getContext(), 12));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        pathEffect = new CornerPathEffect(5);
        mPaint.setPathEffect(pathEffect);
        
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStrokeWidth(8);
        mPathPaint.setColor(Color.GRAY);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        
        mViewPaint = new Paint();
        mViewPaint.setAntiAlias(true);
        mViewPaint.setStrokeWidth(2);
        mViewPaint.setStyle(Paint.Style.STROKE);
        mViewPaint.setColor(Color.GRAY);
        mViewPaint.setPathEffect(new DashPathEffect(new float[]{4 ,4}, 0));
        mViewPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        hight = h/2;
        margin = dip2px(getContext(), 14); 
        mEqRectf = new RectF(
            margin,
            margin * 2,
            width - margin,
            hight / 2);
        mBaRectf = new RectF(
            margin,
            mEqRectf.bottom + margin,
            width - margin,
            hight - margin);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(bands <= 0)
            return;
        if(mPath == null)
            mPath = new  Path();
        else
            mPath.reset();
        mPaint.setColor(Color.GRAY);
        canvas.drawLine(mEqRectf.left, mEqRectf.centerY(), mEqRectf.right, mEqRectf.centerY(), mPaint);
        if(fList != null) {
            fList.clear();
        }
        fList = getControlPoints(mPoints);
        mPath.moveTo(mPoints.get(0).x, mPoints.get(0).y);
        for (int i = 0; i < bands - 1; i++){
            mPath.cubicTo(
                fList.get(i*2).x,fList.get(i*2).y,
                fList.get(i*2+1).x,fList.get(i*2+1).y,
                mPoints.get(i+1).x,mPoints.get(i+1).y
            );
        }
        mPathPaint.setColor(Color.GRAY);
        canvas.drawPath(mPath, mPathPaint);
        
        for(int i = 0; i < bands; i++) {
            mPaint.setColor(Color.GRAY);
            canvas.drawLine(mPoints.get(i).x, mEqRectf.top, mPoints.get(i).x, mEqRectf.bottom, mPaint);
            mPaint.setColor(Color.BLACK);
            canvas.drawLine(mPoints.get(i).x, mPoints.get(i).y, mPoints.get(i).x, mEqRectf.bottom, mPaint);
            if(i == index) {
                canvas.drawLine(mEqRectf.left, mPoints.get(i).y, mEqRectf.right, mPoints.get(i).y, mViewPaint);
                canvas.drawText((isEQ && isMove ? mEqValue[i] / 100 +"dB" : khzs[i] + "Hz"), mPoints.get(i).x, mEqRectf.top - margin, mPaint);
                if(isMove && isEQ) {
                    mPaint.setColor(Color.GRAY);
                    canvas.drawCircle(mPoints.get(i).x, mPoints.get(i).y, 30, mPaint);
                }
                mPaint.setColor(Color.BLACK);
             } else {
                 canvas.drawText(khzs[i] + "Hz", mPoints.get(i).x, mEqRectf.top - margin, mPaint);
             }
            canvas.drawCircle(mPoints.get(i).x, mPoints.get(i).y, 15, mPaint);
        }
        
        canvas.drawLine(mBaRectf.left, (mBaRectf.bottom + mBaRectf.top) / 2, mBaRectf.right, (mBaRectf.bottom +mBaRectf.top) / 2, mPaint);
        if(isMove && !isEQ) {
            mPaint.setColor(Color.GRAY);
            canvas.drawCircle(mBaValue, mBaRectf.centerY(), 30, mPaint);
            mBarRect.set(mBaValue - margin * 2, mBaRectf.centerY() - margin * 4, mBaValue + margin * 2, mBaRectf.centerY() - margin * 2);
            canvas.drawRoundRect(mBarRect, 10, 10, mPaint);
            if(mBarPath != null)
                mBarPath.reset();
            mBarPath.moveTo(mBaValue - margin * 2, mBaRectf.centerY() - margin * 4);
            mBarPath.lineTo(mBaValue - margin * 2, mBaRectf.centerY() - margin * 2);
            mBarPath.lineTo(mBaValue - margin / 2, mBaRectf.centerY() - margin * 2);
            mBarPath.lineTo(mBaValue, mBaRectf.centerY() - margin * 3 / 2);
            mBarPath.lineTo(mBaValue + margin / 2, mBaRectf.centerY() - margin * 2);
            mBarPath.lineTo(mBaValue + margin * 2, mBaRectf.centerY() - margin * 2);
            mBarPath.lineTo(mBaValue + margin * 2, mBaRectf.centerY() - margin * 4);
            mBarPath.close();
            canvas.drawPath(mBarPath, mPaint);
        }
        mPaint.setColor(Color.BLACK);
        canvas.drawCircle(mBaValue, mBaRectf.centerY(), 15, mPaint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(bands <= 0)
                    return false;
                float x = event.getX();
                float y = event.getY();
                if (!checkClickPosition(x,y)){
                    return false;
                }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_MOVE:
                isMove = true;
                if (isEQ) {
                    if (event.getY() <= mEqRectf.top) {
                        mPoints.get(index).y = mEqRectf.top;
                    } else if (event.getY() >= mEqRectf.bottom) {
                        mPoints.get(index).y = mEqRectf.bottom;
                    } else {
                        mPoints.get(index).y = event.getY();
                    }
                    mEqValue[index] = (int)(minEqLevel + (mEqRectf.bottom - mPoints.get(index).y) * ((maxEqLevel - minEqLevel) / mEqRectf.height()));
                    mEqualizer.setBandLevel((short)index, (short)mEqValue[index]);
                } else {
                    if(event.getX() <= mBaRectf.left) {
                        mBaValue = mBaRectf.left;
                    } else if(event.getX() >= mBaRectf.right) {
                        mBaValue = mBaRectf.right;
                    } else {
                        mBaValue = (int)event.getX();
                    }
                    mBassBoost.setStrength((short) (mBaValue - mBaRectf.left * 1000 / mBarRect.width()));
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isMove = false;
                invalidate();
                break;
        }
        return true;
    }

    private boolean checkClickPosition(float x,float y){
        for (int i =0; i < bands; i++){
            if ((x < mPoints.get(i).x + 40 && x > mPoints.get(i).x - 40) && (y < mEqRectf.bottom && y > mEqRectf.top)){
                index = i;
                isEQ = true;
                return true;
            }
        }
        if(y < mBaRectf.bottom && y > mBaRectf.top) {
            isEQ = false;
            return true;
        }
        return false;
    }

    private List<PointF> getControlPoints(List<PointF> points){
        // 计算斜率 y = kx + b => k = (y - b)/x
        if (points.size() < 3){
            return null;
        }
        List<PointF> pointFList = new ArrayList<>();
        float rate = 0.3f;
        //从第一个控制点开始计算
        float cx1 = points.get(0).x + (points.get(1).x - points.get(0).x) * rate;
        float cy1 = points.get(0).y;
        pointFList.add(new PointF(cx1,cy1));

        for (int i =1;i<points.size()-1;i++){
            //第二个点
            float k = (points.get(i+1).y - points.get(i-1).y)/(points.get(i+1).x - points.get(i-1).x);
            float b = points.get(i).y - k*points.get(i).x;
            //左边控制点
            float cxLeft = points.get(i).x - (points.get(i).x - points.get(i-1).x)*rate;
            float cyLeft = k*cxLeft + b;
            pointFList.add(new PointF(cxLeft,cyLeft));
            //右边控制点
            float cxRight = points.get(i).x + (points.get(i+1).x - points.get(i).x)*rate;
            float cyRight = k*cxRight + b;
            pointFList.add(new PointF(cxRight,cyRight));
        }
        //最后一个点
        float cxLast = points.get(points.size() - 1).x - (points.get(points.size() - 1).x - points.get(points.size() - 2).x)*rate;
        float cyLast = points.get(points.size() - 1).y;
        pointFList.add(new PointF(cxLast,cyLast));
        return pointFList;
    }

    public void setAudioSessionId(int audioSessionId) {
        if(mEqualizer != null)
            mEqualizer.release();
        mEqualizer = new Equalizer(0, audioSessionId);
        mEqualizer.setEnabled(true);
        minEqLevel = mEqualizer.getBandLevelRange()[0];
        maxEqLevel = mEqualizer.getBandLevelRange()[1];
        bands = mEqualizer.getNumberOfBands();
        if(khzs == null)
            khzs = new int[bands];
        if(mEqValue == null)
            mEqValue = new int[bands];
        for(short i = 0; i < bands; i++) {
            khzs[i] = mEqualizer.getCenterFreq(i) / 1000;
            mEqValue[i] = (int)mEqRectf.centerY();
            mPoints.add(new PointF(mEqRectf.width() / 5 * i + mEqRectf.left + mEqRectf.width() / 5 / 2, mEqValue[i]));
        }
        mBassBoost = new BassBoost(0, audioSessionId);
        mBassBoost.setEnabled(true);
        invalidate();
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
