package com.wei.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PorterDuff;
import android.view.View;

public class VisualizerView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private SurfaceHolder mSurfaceHolder = null;
    private int LINE_MAX_COUNT = 240;
    private int LINE_MIN_COUNT = 3;
    private float DENSITY = 0.2f;
    private int LINE_COUNT = (int) (LINE_MAX_COUNT * DENSITY);
    private int LINE_WIDTH = 6;
    private int LINE_COLOR = Color.WHITE;

    private int RADIUS;
    private int POINT_RADIUS;

    private float[] lastWave, nowWave, toWave, drawWave;
    private float[] lastFFT, nowFFT, toFFT, drawFFT;
    private Paint mWavePaint, mLinePaint;
    private Object synObject = new Object();

    private int mFrameNumber;
    private int mFrame;
    private int fps = 60;
    private int perFrameTime = 1000 / fps;

    private boolean runFlag;
    private Thread mDrawThread;
    private long mStarDrawTime;
    private int mLastFrame;

    private Visualizer mVisualizer;

    private ShowStyle mShowStyle = ShowStyle.STYLE_CIRCLE;

    private Path mPath;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        drawWave = new float[LINE_COUNT + 1];
        drawFFT = new float[LINE_COUNT + 1];

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mLinePaint.setColor(LINE_COLOR);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2);  
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setDither(true);
        mWavePaint.setColor(LINE_COLOR);
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setStrokeWidth(2);  
        mWavePaint.setStrokeCap(Paint.Cap.ROUND);

        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        if (!isInEditMode())
            setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        RADIUS = Math.min(w, h) / 4;
        POINT_RADIUS = Math.abs((int) (2 * RADIUS * Math.sin(Math.PI / LINE_COUNT / 3)));
    }  

    private void setToFft(byte[] fft) {
        if (toFFT == null) {
            toFFT = new float[LINE_COUNT + 1];
            nowFFT = new float[LINE_COUNT + 1];
            drawFFT = new float[LINE_COUNT + 1];
        }
        for (int i = 0; i < LINE_COUNT + 1; i++) {
            toFFT[i] = (float) Math.abs(Math.hypot(fft[i], fft[i+1]));
        }
        lastFFT = nowFFT.clone();
    }

    private void setToWave(byte[] wave) {
        if (toWave == null) {
            toWave = new float[LINE_COUNT + 1];
            nowWave = new float[LINE_COUNT + 1];
            drawWave = new float[LINE_COUNT + 1];
        }
        for (int i = 0; i < LINE_COUNT + 1; i++) {
            int x = (int) Math.ceil((i + 1) * (wave.length / LINE_COUNT));
            int t = 0;
            if (x < 1024) {
                t = (byte)(Math.abs(wave[x]) + 128) * RADIUS / 128;
            }
            toWave[i] = -t;
        }
        toWave = SmoothFilter.cubicSmooth7(toWave);
        lastWave = nowWave.clone();
    }

    public void updateWithAmin(int rate, byte[] fft, byte[] waveform) {
        synchronized (synObject) {
            if(fft != null)
                setToFft(fft);
            if(waveform != null)
                setToWave(waveform);
            mFrameNumber = fps * 1000 / rate;
            mStarDrawTime = System.currentTimeMillis();
            mFrame = 1;
        }
    }

    public void setAudioSessionId(int audioSessionId) {
        final int rate = Visualizer.getMaxCaptureRate() / 4;
        if (mVisualizer != null)
            release();
        mVisualizer = new Visualizer(audioSessionId);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                byte[] wave = null, fft = null;
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                  int samplingRate) {
                    if (mShowStyle == ShowStyle.STYLE_CIRCLE)
                        this.wave = bytes;
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                             int samplingRate) {
                    if(mShowStyle == ShowStyle.STYLE_LINE_BAR || mShowStyle == ShowStyle.STYLE_LINE_BAR_AND_WAVE)
                        this.fft = bytes;
                    updateWithAmin(rate, this.fft, this.wave);
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, true);
    }

    public void drawWaveAndFFT(Canvas canvas) {
        if(!runFlag) return;
        if (mShowStyle == ShowStyle.STYLE_CIRCLE) {
            for (int i = 0; i < 360; i = i + 360 / LINE_COUNT + 1) {
                float cx = (float) (getWidth() / 2 + Math.cos(i * Math.PI / 180) * RADIUS);
                float cy = (float) (getHeight() / 2 - Math.sin(i * Math.PI / 180) * RADIUS);
                canvas.drawCircle(cx, cy, POINT_RADIUS, mLinePaint);
            }
            for (int i = 0; i < 360; i = i + 360 / LINE_COUNT + 1) {
                if (drawWave == null) continue;
                canvas.save();
                canvas.rotate(-i, getWidth() / 2, getHeight() / 2);
                canvas.drawRect(getWidth() / 2 + RADIUS, getHeight() / 2 - POINT_RADIUS, getWidth() / 2 + RADIUS + drawWave[i * LINE_COUNT / 360],
                                getHeight() / 2 + POINT_RADIUS, mLinePaint);
                canvas.drawCircle(getWidth() / 2 + RADIUS + drawWave[i * LINE_COUNT / 360], getHeight() / 2, POINT_RADIUS, mLinePaint);
                canvas.restore();
            }
        }
        if (mShowStyle == ShowStyle.STYLE_LINE_BAR | mShowStyle == ShowStyle.STYLE_LINE_BAR_AND_WAVE) {
            for (int i = 0; i < LINE_COUNT + 1; i++) {
                canvas.drawRect((getWidth() / LINE_COUNT) * i + LINE_WIDTH,
                                (getHeight() * 2/3) - drawFFT[i] - LINE_WIDTH,
                                (getWidth() / LINE_COUNT) * (i + 1),
                                getHeight() * 2/3,
                                mLinePaint);
            }
        }
        if (mShowStyle == ShowStyle.STYLE_LINE_BAR_AND_WAVE) {
            if (mPath == null)
                mPath = new Path();
            else
                mPath.reset();
            for (int i = 0; i < LINE_COUNT + 1; i++) {
                int midX = ((getWidth() / (LINE_COUNT)) * i + (getWidth() / LINE_COUNT) * (i + 1)) >> 1;
                if (i == 0) {
                    mPath.moveTo(LINE_WIDTH, getHeight() * 2/3);
                } else if (i == LINE_COUNT) {
                    mPath.cubicTo(midX + LINE_WIDTH * 3 / 2, getHeight() * 2/3,
                                  midX + LINE_WIDTH * 3 / 2, getHeight() * 2/3,
                                  (getWidth() / LINE_COUNT) * (i + 1) - LINE_WIDTH, getHeight() * 2/3);

                } else {
                    mPath.cubicTo(midX + LINE_WIDTH * 3 / 2, getHeight() * 2/3 + drawFFT[i],
                                  midX + LINE_WIDTH * 3 / 2, getHeight() * 2/3 + drawFFT[i + 1],
                                  (getWidth() / LINE_COUNT) * (i + 1) + LINE_WIDTH * 3 / 2, getHeight() * 2/3 + drawFFT[i + 1]);
                }
                canvas.drawPath(mPath, mWavePaint);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        draw();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        runFlag = false;
    }

    public void draw() {
        Canvas canvas;
        if (mSurfaceHolder == null || (canvas = mSurfaceHolder.lockCanvas()) == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawWaveAndFFT(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void calNowDataAndDraw() {
        boolean isDraw = true;
        mFrame = (int) ((System.currentTimeMillis() - mStarDrawTime) / perFrameTime) + 1;
        mFrame = Math.min(mFrame, mFrameNumber);
        if (mLastFrame == mFrame) {
            try {
                Thread.sleep(perFrameTime / 4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        synchronized (synObject) {
            if (mFrame < mFrameNumber) {
                float bl = (float) mFrame / mFrameNumber;
                if (toFFT != null) {
                    for (int i = 0; i < LINE_COUNT + 1; i++) {
                        nowFFT[i] = lastFFT[i] + ((toFFT[i] - lastFFT[i]) * bl);
                    }
                    drawFFT = nowFFT.clone();
                }
                if (toWave != null) {
                    for (int i = 0; i < LINE_COUNT + 1; i++) {
                        nowWave[i] = lastWave[i] + ((toWave[i] - lastWave[i]) * bl);
                    }
                    drawWave = nowWave.clone();
                }
            } else if (mFrame == mFrameNumber) {
                if (mShowStyle == ShowStyle.STYLE_LINE_BAR || mShowStyle == ShowStyle.STYLE_LINE_BAR_AND_WAVE) {
                    if(toFFT == null)
                        return;
                    nowFFT = toFFT.clone();
                    drawFFT = nowFFT.clone();
                } else {
                    if(toWave == null)
                        return;
                    nowWave = toWave.clone();
                    drawWave = nowWave.clone();
                }
            } else {
                isDraw = false;
            }
            mLastFrame = mFrame;
        }
        if (isDraw)
            draw();
    }

    @Override
    public void run() {
        while (runFlag) {
            if (toFFT != null || toWave != null) {
                calNowDataAndDraw();
            }
        }
        if (toFFT != null)
            drawFFT = new float[LINE_COUNT + 1];
        if (toWave != null)
            drawWave = new float[LINE_COUNT + 1];
        draw();
    }

    public void stop() {
        runFlag = false;
        mVisualizer.setEnabled(false);
    }

    public void start() {
        if (runFlag == false) {
            if (mDrawThread != null && mDrawThread.isAlive()) {
                try {
                    mDrawThread.join(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mVisualizer.setEnabled(true);
            runFlag = true;
            mDrawThread = new Thread(this);
            mDrawThread.start();
        }
    }

    public void setFps(int fps) {
        this.fps = fps;
        this.perFrameTime = 1000 / fps;
    }

    public void setLineWidth(int width) {
        this.LINE_WIDTH = width;
    }

    public void setLineBarColor(int color) {
        this.LINE_COLOR = color;
        this.mLinePaint.setColor(color);
        this.mWavePaint.setColor(color);
    }

    public void setLineStrokeWidth(int width) {
        this.mLinePaint.setStrokeWidth(width);
    }

    public void setLineStyle(Paint.Style style) {
        this.mLinePaint.setStyle(style);
    }

    public void setWaveStyle(Paint.Style style) {
        this.mWavePaint.setStyle(style);
    }

    public void setShowStyle(ShowStyle style) {
        this.mShowStyle = style;
        init();
    }

    public void release() {
        if(mVisualizer != null)
            mVisualizer.release();
    }

    public enum ShowStyle {
        STYLE_LINE_BAR,
        STYLE_LINE_BAR_AND_WAVE,
        STYLE_CIRCLE,
    }
}

