package com.shopnum1.distributionportal.util;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class CommonUtility {

	
/**
     * 读取图片属性：旋转的角度
     * @param path 图片绝对路径
     * @return degree 旋转的角度
     */
	public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
        ExifInterface exifInterface = new ExifInterface(path);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
        case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
        case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return degree;
    }      
	/** 
     * 旋转图片，使图片保持正确的方向。 
     * @param bitmap 原始图片 
     * @param degrees 原始图片的角度 
     * @return Bitmap 旋转后的图片 
     */  
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {  
        if (degrees == 0 || null == bitmap) {  
            return bitmap;  
        }  
        Matrix matrix = new Matrix();  
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);  
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
        if (null != bitmap) {  
            bitmap.recycle();  
        }  
        return bmp;  
    }
    /** 计算图片缩放比例*/
    public static int calculateInSampleSize(BitmapFactory.Options options,  
            int reqWidth, int reqHeight) {  
        // Raw height and width of image  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
  
        if (height > reqHeight || width > reqWidth) {  
  
            // Calculate ratios of height and width to requested height and width  
            final int heightRatio = Math.round((float) height / (float) reqHeight);  
            final int widthRatio = Math.round((float) width / (float) reqWidth);  
  
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;  
        }
        return inSampleSize;  
    }

}
