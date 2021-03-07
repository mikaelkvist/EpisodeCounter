package com.example.episode_counter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class BitmapConversions {

    public static final String EMPTY_IMAGE_STRING = "";


    public static String getImageAsString(Context context, String title) {
        File file = new File(context.getFilesDir(), title);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap != null) {
            return convertBitmapToString(bitmap);
        } else {
            return EMPTY_IMAGE_STRING;
        }
    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    public static Bitmap convertStringToBitMap(String str) {
        if (str.equals(EMPTY_IMAGE_STRING)) {
            return null;
        } else {
            try {
                byte[] encodedBytes = Base64.decode(str, Base64.NO_WRAP);
                return BitmapFactory.decodeByteArray(encodedBytes, 0, encodedBytes.length);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

    }
}
