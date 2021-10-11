package com.wei.music.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.core.graphics.drawable.DrawableCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wei.music.R;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

 
public class GlideLoadUtils {
    
    /**
     * 借助内部类 实现线程安全的单例模式
     * 属于懒汉式单例，因为Java机制规定，内部类SingletonHolder只有在getInstance()
     * 方法第一次调用的时候才会被加载（实现了lazy），而且其加载过程是线程安全的。
     * 内部类加载的时候实例化一次instance。
     */
    public GlideLoadUtils() {
    }

    private static class GlideLoadUtilsHolder {
        private final static GlideLoadUtils GlideLoadUtilINSTANCE = new GlideLoadUtils();
    }

    public static GlideLoadUtils getInstance() {
        return GlideLoadUtilsHolder.GlideLoadUtilINSTANCE;
    }
    
    public static Drawable setDrawableColor(Drawable drawable, int colorResId){
        Drawable modeDrawable = drawable.mutate();
        Drawable temp = DrawableCompat.wrap(modeDrawable);
        DrawableCompat.setTint(temp,colorResId);
        return temp;
    }
    
    //设置圆图
    public static void setCircle(Context context, String url, ImageView view) {
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_music)
                .crossFade()
                .bitmapTransform(new CenterCrop(context), new CropCircleTransformation(context))
                .into(view); 
        }
    }

    //设置圆图
    public static void setCircle(Context context, int bitmap, ImageView view) {
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(bitmap)
                .placeholder(R.drawable.ic_music_load)
                .error(R.drawable.ic_music_load)
                .crossFade()
                .bitmapTransform(new CenterCrop(context), new CropCircleTransformation(context))
                .into(view);
        }  
    }
    
    //设置圆角图
    public static void setRound(Context context, String url, int round, int blur, ImageView view) {
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .placeholder(R.drawable.ic_music_load)
                .error(R.drawable.ic_music_load)
                .crossFade()
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, dip2px(context, round), 0), new BlurTransformation(context, blur))
                .into(view);
        }   
    }

    //设置圆角图
    public static void setRound(Context context, int bitmap, int round, ImageView view) {
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(bitmap)
                .placeholder(R.drawable.ic_music_load)
                .error(R.drawable.ic_music_load)
                .crossFade()
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, dip2px(context, round), 0))
                .into(view);
        }   
    }
    
    //设置圆角图
    public static void setRound(Context context, String url, int round, ImageView view) {
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .placeholder(R.drawable.ic_music_load)
                .error(R.drawable.ic_music_load)
                .crossFade()
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, dip2px(context, round), 0))
                .into(view);
        }   
    }

    //设置圆角图
    public static void setRound(Context context, int bitmap, int round, int blur, ImageView view) {
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(bitmap)
                .placeholder(R.drawable.ic_music_load)
                .error(R.drawable.ic_music_load)
                .crossFade()
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, dip2px(context, round), 0), new BlurTransformation(context, blur))
                .into(view);
        }   
    }
    
    public static void getBitmap(Context context, String url, SimpleTarget<Bitmap> mSimpleTarget) {       
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .asBitmap()
                .into(mSimpleTarget);
        }
    }
    
    public static void getBitmap(Context context, String url, int blur, SimpleTarget<Bitmap> mSimpleTarget) {       
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .asBitmap()
                .transform(new BlurTransformation(context, blur))
                .into(mSimpleTarget);
        }
    }
    
    public static void getBitmap(Context context, String url, int round, int blur, SimpleTarget<Bitmap> mSimpleTarget) {       
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .asBitmap()
                .transform(new RoundedCornersTransformation(context, dip2px(context, round), 0), new BlurTransformation(context, blur))
                .into(mSimpleTarget);
        }
    }

    public static void getBitmap(Context context, int url, int round, int blur, SimpleTarget<Bitmap> mSimpleTarget) {       
        if (!((Activity)context).isDestroyed()) {
            Glide.with((Activity)context)
                .load(url)
                .asBitmap()
                .transform(new RoundedCornersTransformation(context, dip2px(context, round), 0), new BlurTransformation(context, blur))
                .into(mSimpleTarget);
        }
    }

    private static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
    
    
}
