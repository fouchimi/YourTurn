package com.social.yourturn.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by ousma on 5/2/2017.
 */

public abstract class ShaderImageView extends android.support.v7.widget.AppCompatImageView {

    private final static boolean DEBUG = false;
    private ShaderHelper pathHelper;


    public ShaderImageView(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public ShaderImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public ShaderImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, AttributeSet attrs, int defStyleAttr){
        getPathHelper().init(context, attrs, defStyleAttr);
    }

    protected ShaderHelper getPathHelper(){
        if(pathHelper == null){
            pathHelper = createImageViewHelper();
        }
        return  pathHelper;
    }

    protected abstract ShaderHelper createImageViewHelper();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getPathHelper().isSquare()) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getPathHelper().onSizeChanged(w, h);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        getPathHelper().onImageDrawableReset(getDrawable());
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        getPathHelper().onImageDrawableReset(getDrawable());
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        getPathHelper().onImageDrawableReset(getDrawable());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(DEBUG) canvas.drawRGB(10, 200, 200);

        if(!getPathHelper().onDraw(canvas)){
            super.onDraw(canvas);
        }
    }

    public int getBorderWidth(){
        return getPathHelper().getBorderWidth();
    }

    public void setBorderWidth(final int borderWidth){
        getPathHelper().setBorderWidth(borderWidth);
        invalidate();
    }

    public float getBorderAlpha(){
        return getPathHelper().getBorderAlpha();
    }

    public void setBorderAlpha(final float borderAlpha){
        getPathHelper().setBorderAlpha(borderAlpha);
        invalidate();
    }

    public void setSquare(final boolean square){
        getPathHelper().setSquare(square);
        invalidate();
    }
}
