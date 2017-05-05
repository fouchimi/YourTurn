package com.social.yourturn.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;

/**
 * Created by ousma on 5/2/2017.
 */

public abstract class ShaderHelper {

    private final static int ALPHA_MAX = 255;
    private static final float SHADE_FACTOR = 0.9f;

    protected int viewWidth;
    protected int viewHeight;

    protected int borderColor = Color.BLACK;
    protected int borderWidth = 0;
    protected float borderAlpha = 1f;
    protected boolean square;

    protected final Paint borderPaint;
    protected final Paint imagePaint;
    protected final Paint textPaint;
    protected final Paint textBorderPaint;
    protected BitmapShader shader;
    protected Drawable drawable;
    protected final Matrix matrix = new Matrix();

    public ShaderHelper() {
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        imagePaint = new Paint();
        imagePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStrokeWidth(0f);

        textBorderPaint = new Paint();
        textBorderPaint.setColor(getDarkerShade(Color.DKGRAY));
        textBorderPaint.setStyle(Paint.Style.STROKE);
        textBorderPaint.setStrokeWidth(1f);
    }

    public void init(Context context, AttributeSet attrs, int defStyle){
        if(attrs == null){
            borderColor = context.getResources().getColor(android.R.color.darker_gray);
            borderWidth = Math.round(4f * context.getResources().getDisplayMetrics().density);
            borderAlpha = Math.round(borderAlpha * 0.5);
            square = false;
        }

    }

    public abstract void draw(Canvas canvas, Paint imagePaint, Paint borderPaint);
    public abstract void reset();
    public abstract void calculate(int bitmapWidth, int bitmapHeight, float width, float height, float scale, float translateX, float translateY);

    public final void onImageDrawableReset(Drawable drawable) {
        if(drawable instanceof BitmapDrawable){
            this.drawable = drawable;
            shader = null;
            imagePaint.setShader(null);
        }else {
            this.drawable = new ShapeDrawable();
        }
    }

    public void onSizeChanged(int width, int height) {
        if(viewWidth == width && viewHeight == height) return;
        viewWidth = width;
        viewHeight = height;
        if(isSquare()) {
            viewWidth = viewHeight = Math.min(width, height);
        }
        if(shader != null) {
            calculateDrawableBitmapSize();
        }
    }

    public Bitmap calculateDrawableBitmapSize() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            if(bitmapWidth > 0 && bitmapHeight > 0) {
                float width = Math.round(viewWidth - 2f * borderWidth);
                float height = Math.round(viewHeight - 2f * borderWidth);

                float scale;
                float translateX = 0;
                float translateY = 0;

                if (bitmapWidth * height > width * bitmapHeight) {
                    scale = height / bitmapHeight;
                    translateX = Math.round((width/scale - bitmapWidth) / 2f);
                } else {
                    scale = width / (float) bitmapWidth;
                    translateY = Math.round((height/scale - bitmapHeight) / 2f);
                }

                matrix.setScale(scale, scale);
                matrix.preTranslate(translateX, translateY);
                matrix.postTranslate(borderWidth, borderWidth);

                calculate(bitmapWidth, bitmapHeight, width, height, scale, translateX, translateY);

                return bitmap;
            }
        }

        reset();
        return null;
    }

    public boolean onDraw(Canvas canvas) {
        if (shader == null) {
            createShader();
        }
        if (shader != null && viewWidth > 0 && viewHeight > 0) {
            draw(canvas, imagePaint, borderPaint);
            return true;
        }
        return false;
    }

    private int getDarkerShade(int color){
        return Color.rgb((int) (SHADE_FACTOR * Color.red(color)),
                (int)(SHADE_FACTOR * Color.green(color)),
                (int)(SHADE_FACTOR * Color.blue(color)));
    }

    protected void createShader() {
        Bitmap bitmap = calculateDrawableBitmapSize();
        if(bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            imagePaint.setShader(shader);
        }
    }

    protected Bitmap getBitmap() {
        Bitmap bitmap = null;
        if(drawable instanceof BitmapDrawable){
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }else {
            final int width = !drawable.getBounds().isEmpty() ? drawable.getBounds().width() : drawable.getIntrinsicWidth();
            final int height = !drawable.getBounds().isEmpty() ? drawable.getBounds().height() : drawable.getIntrinsicHeight();

            bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);
        }
        return bitmap;
    }


    public final void setBorderColor(final int borderColor) {
        this.borderColor = borderColor;
        if(borderPaint != null) {
            borderPaint.setColor(borderColor);
        }
    }

    public final int getBorderWidth() {
        return borderWidth;
    }

    public final void setBorderWidth(final int borderWidth) {
        this.borderWidth = borderWidth;
        if(borderPaint != null) {
            borderPaint.setStrokeWidth(borderWidth);
        }
    }

    public final float getBorderAlpha() {
        return borderAlpha;
    }

    public final void setBorderAlpha(final float borderAlpha) {
        this.borderAlpha = borderAlpha;
        if(borderPaint != null) {
            borderPaint.setAlpha(Float.valueOf(borderAlpha * ALPHA_MAX).intValue());
        }
    }

    public final void setSquare(final boolean square) {
        this.square = square;
    }

    public final boolean isSquare() {
        return square;
    }

}
