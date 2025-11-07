package com.olam.digital.ofis.mvvmmodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import com.olam.digital.ofis.Utilities;

import java.io.IOException;
import java.io.InputStream;

public class ImageOrientation {

    public static Bitmap modifyOrientation(Context context, Bitmap bitmap, Uri uri) {
        try (InputStream exifStream = context.getContentResolver().openInputStream(uri)) {
            if (exifStream != null) {
                ExifInterface ei = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ei = new ExifInterface(exifStream);
                }

                if (ei != null) {
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    return switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90);
                        case ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180);
                        case ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270);
                        case ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false);
                        case ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true);
                        default -> bitmap;
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}