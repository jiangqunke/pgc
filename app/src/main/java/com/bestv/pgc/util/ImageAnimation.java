package com.bestv.pgc.util;

import android.graphics.drawable.Animatable;
import android.widget.ImageView;


public class ImageAnimation {
    /**
     * 示例   ImageAnimation imageAnimation=new ImageAnimation(image_give);
     * imageAnimation.giveStart();
     * <p>
     * image_give imageview控件
     * <p>
     * <ImageView
     * android:id="@+id/image_give"
     * android:background="@drawable/giveorange_show"
     * android:layout_marginTop="@dimen/dp_40"
     * android:layout_width="@dimen/dp_50"
     * android:layout_height="@dimen/dp_50">
     * </ImageView>
     */
    private static Animatable animatable;

//    public ImageAnimation(ImageView imageView) {
//        try {
//            animationDrawable = (AnimationDrawable) imageView.getBackground();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void giveStart(ImageView imageView) {
        animatable = (Animatable) imageView.getDrawable();
        if (animatable != null) {
            animatable.start();
        }
    }

    public static void giveStop(ImageView imageView) {
        animatable = (Animatable) imageView.getDrawable();
        if (animatable != null && animatable.isRunning()) {
            animatable.stop();
        }

    }
}

