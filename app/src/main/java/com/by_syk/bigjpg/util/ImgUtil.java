package com.by_syk.bigjpg.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by By_syk on 2017-06-20.
 */

public class ImgUtil {
    @Nullable
    public static Point getImgSize(Context context, Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri), null, options);
            return new Point(options.outWidth, options.outHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
