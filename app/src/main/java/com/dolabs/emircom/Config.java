package com.dolabs.emircom;

import android.graphics.Bitmap;

public class Config {

    public static final boolean IS_PRODUCTION = false;

    public static final int IMAGE_SIZE = 1920;
    public static final int IMAGE_SIZE_HALF = 512;
    public static final Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.JPEG;
    public static final int IMAGE_QUALITY = 50;
    public static final int IMAGE_QUALITY_FULL = 100;
    public static final int IMAGE_MAX_SIZE = 524288; //512KiB
}
