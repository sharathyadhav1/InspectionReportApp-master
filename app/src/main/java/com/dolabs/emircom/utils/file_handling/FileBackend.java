package com.dolabs.emircom.utils.file_handling;



import static com.dolabs.emircom.utils.file_handling.FileUtils.FOLDER_PROFILE;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;

import androidx.core.content.FileProvider;


import com.dolabs.emircom.ApplicationClass;
import com.dolabs.emircom.Config;
import com.dolabs.emircom.R;
import com.dolabs.emircom.session.SessionContext;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class FileBackend {

    private static final String TAG = FileBackend.class.getSimpleName();
    private static final Object THUMBNAIL_LOCK = new Object();

    private static final SimpleDateFormat IMAGE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    public static final String FILE_PROVIDER = ".files";

    private final Context mContext;

    public FileBackend(Context service) {
        this.mContext = service;
    }

    public static void CreateUpdateNoMedia() {

        final File rootFolder = new File(FileUtils.getAppRootFolder());

        try {

            if(!rootFolder.exists()) {
                rootFolder.mkdirs();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        final File nomedia = new File(rootFolder,  ".nomedia");

        if (!nomedia.exists()) {

            try {

                nomedia.createNewFile();
            }
            catch (Exception e) {

                Log.d(TAG, "could not create nomedia file");
            }
        }
    }

    public static void updateMediaScanner(File file) {

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        SessionContext.mContext.sendBroadcast(intent);
    }

    private static long getFileSize(Context context, Uri uri) {

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        } else {
            return -1;
        }
    }

    public static boolean allFilesUnderSize(Context context, List<Uri> uris, long max) {
        if (max <= 0) {
            Log.d(TAG, "server did not report max file size for http upload");
            return true; //exception to be compatible with HTTP Upload < v0.2
        }
        for (Uri uri : uris) {
            if (getFileSize(context, uri) > max) {
                Log.d(TAG, "not all files are under " + max + " bytes. suggesting falling back to jingle");
                return false;
            }
        }
        return true;
    }

    public static String getAppDirectory(final String type) {
        return FileUtils.getAppDirectory(type);
    }

    public Bitmap resize(Bitmap originalBitmap, int size) {

        int w = originalBitmap.getWidth();
        int h = originalBitmap.getHeight();

        if (Math.max(w, h) > size) {

            int scalledW;
            int scalledH;

            if (w <= h) {
                scalledW = (int) (w / ((double) h / size));
                scalledH = size;
            }
            else {
                scalledW = size;
                scalledH = (int) (h / ((double) w / size));
            }

            Bitmap result = Bitmap.createScaledBitmap(originalBitmap, scalledW, scalledH, true);

            if (originalBitmap != null && !originalBitmap.isRecycled()) {
                originalBitmap.recycle();
            }

            return result;
        }
        else {

            return originalBitmap;
        }

    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        if (degree == 0) {
            //degree = 90;
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(degree);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return result;
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public boolean useImageAsIs(Uri uri) {
        String path = getOriginalPath(uri);
        if (path == null) {
            return false;
        }
        File file = new File(path);
        long size = file.length();
        if (size == 0 || size >= Config.IMAGE_MAX_SIZE) {
            return false;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri), null, options);
            if (options == null || options.outMimeType == null || options.outHeight <= 0 || options.outWidth <= 0) {
                return false;
            }
            return (options.outWidth <= Config.IMAGE_SIZE && options.outHeight <= Config.IMAGE_SIZE && options.outMimeType.contains(Config.IMAGE_FORMAT.name().toLowerCase()));
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public String getOriginalPath(Uri uri) {
        return FileUtils.getPath(mContext, uri);
    }

    public void copyFileToPrivateStorage(File file, Uri uri) throws FileCopyException {

        Log.d(TAG, "copy file (" + uri.toString() + ") to private storage " + file.getAbsolutePath());
        file.getParentFile().mkdirs();
        OutputStream os = null;
        InputStream is = null;

        try {

            file.createNewFile();
            os = new FileOutputStream(file);

            boolean renameSuccess = false;
            File file1 = null;

            try {

                if(uri.toString().contains("content://")) {

                    file1 = new File(FileUtils.getPath(mContext, uri));
                }
                else {

                    file1 = new File(uri.getPath());
                }
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            if(file1 == null) {

                is = mContext.getContentResolver().openInputStream(uri);
            }
            else {

                if(uri.toString().contains(ApplicationClass.getAppName() + "/Media")) {

                    renameSuccess = file1.renameTo(file);
                }

                if(!renameSuccess) {

                    is = new FileInputStream(file1);
                }
            }

            if(!renameSuccess) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    try {
                        os.write(buffer, 0, length);
                    } catch (IOException e) {
                        throw new FileWriterException();
                    }
                }
            }

            try {
                os.flush();
            } catch (IOException e) {
                throw new FileWriterException();
            }
        } catch (FileNotFoundException e) {
            throw new FileCopyException(R.string.error_file_not_found);
        } catch (FileWriterException e) {
            throw new FileCopyException(R.string.error_unable_to_create_temporary_file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileCopyException(R.string.error_io_exception);
        } finally {
            close(os);
            close(is);
        }
    }

    private String getExtensionFromUri(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        String filename = null;
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    filename = cursor.getString(0);
                }
            } catch (Exception e) {
                filename = null;
            } finally {
                cursor.close();
            }
        }
        int pos = filename == null ? -1 : filename.lastIndexOf('.');
        return pos > 0 ? filename.substring(pos + 1) : null;
    }

    public void copyImageToPrivateStorage(File file, Uri image) throws FileCopyException {
        Log.d(TAG, "copy image (" + image.toString() + ") to private storage " + file.getAbsolutePath());
        copyImageToPrivateStorage(file, image, 0);
    }

    private synchronized void copyImageToPrivateStorage(File file, Uri image, int sampleSize) throws FileCopyException {

        file.getParentFile().mkdirs();
        InputStream is = null;
        OutputStream os = null;

        try {

            if (!file.exists() && !file.createNewFile()) {
                throw new FileCopyException(R.string.error_unable_to_create_temporary_file);
            }

            try {

                is = mContext.getContentResolver().openInputStream(image);
            }
            catch (Exception e) {

                //e.printStackTrace();

                File file1 = new File(image.getPath());
                is = new FileInputStream(file1);
            }

            if (is == null) {
                throw new FileCopyException(R.string.error_not_an_image_file);
            }

            Bitmap originalBitmap;
            BitmapFactory.Options options = new BitmapFactory.Options();

            int inSampleSize = (int) Math.pow(2, sampleSize);
            Log.d(TAG, "reading bitmap with sample size " + inSampleSize);

            options.inSampleSize = inSampleSize;
            originalBitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();

            if (originalBitmap == null) {
                throw new FileCopyException(R.string.error_not_an_image_file);
            }

            Bitmap scaledBitmap = resize(originalBitmap, Config.IMAGE_SIZE);
            int rotation = getRotation(image);

            /*if(rotation == 0)
                rotation = 90;*/

            scaledBitmap = rotate(scaledBitmap, rotation);
            boolean targetSizeReached = false;
            int quality = Config.IMAGE_QUALITY;

            while (!targetSizeReached) {

                os = new FileOutputStream(file);
                boolean success = scaledBitmap.compress(Config.IMAGE_FORMAT, quality, os);
                if (!success) {
                    throw new FileCopyException(R.string.error_compressing_image);
                }
                os.flush();
                targetSizeReached = file.length() <= Config.IMAGE_MAX_SIZE || quality <= 50;
                quality -= 5;
            }

            scaledBitmap.recycle();
        } catch (FileNotFoundException e) {
            throw new FileCopyException(R.string.error_file_not_found);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileCopyException(R.string.error_io_exception);
        } catch (SecurityException e) {
            throw new FileCopyException(R.string.error_security_exception_during_image_copy);
        } catch (OutOfMemoryError e) {

            ++sampleSize;

            if (sampleSize <= 3) {
                copyImageToPrivateStorage(file, image, sampleSize);
            } else {
                throw new FileCopyException(R.string.error_out_of_memory);
            }
        } finally {

            close(os);
            close(is);
        }
    }

    private void copyFileToPrivateStorageWithoutScaling(File file, Uri fileUri) throws FileCopyException {

        file.getParentFile().mkdirs();
        InputStream is = null;
        FileOutputStream os = null;

        try {

            if (!file.exists() && !file.createNewFile()) {
                throw new FileCopyException(R.string.error_unable_to_create_temporary_file);
            }

            is = mContext.getContentResolver().openInputStream(fileUri);
            os = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                os.write(buffer, 0, len1);
            }
        } catch (Exception e) {
            throw new FileCopyException(R.string.error_file_not_found);
        } finally {

            close(os);
            close(is);
        }
    }

    private int getRotation(File file) {
        return getRotation(Uri.parse("file://" + file.getAbsolutePath()));
    }

    private int getRotation(Uri image) {
        InputStream is = null;
        try {
            is = mContext.getContentResolver().openInputStream(image);
            return ExifHelper.getOrientation(is);
        } catch (FileNotFoundException e) {
            return 0;
        } finally {
            close(is);
        }
    }

    private Bitmap getFullsizeImagePreview(File file, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calcSampleSize(file, size);
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (OutOfMemoryError e) {
            options.inSampleSize *= 2;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
    }

    public void drawOverlay(Bitmap bitmap, int resource, float factor) {
        Bitmap overlay = BitmapFactory.decodeResource(mContext.getResources(), resource);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        float targetSize = Math.min(canvas.getWidth(), canvas.getHeight()) * factor;
        Log.d(TAG, "target size overlay: " + targetSize + " overlay bitmap size was " + overlay.getHeight());
        float left = (canvas.getWidth() - targetSize) / 2.0f;
        float top = (canvas.getHeight() - targetSize) / 2.0f;
        RectF dst = new RectF(left, top, left + targetSize - 1, top + targetSize - 1);
        canvas.drawBitmap(overlay, null, dst, paint);
    }

    public Bitmap getVideoPreview(File file, int size) {

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        Bitmap frame;

        try {

            metadataRetriever.setDataSource(file.getAbsolutePath());
            frame = metadataRetriever.getFrameAtTime(0);
            metadataRetriever.release();
            frame = resize(frame, size);
        }
        catch (Exception e) {

            frame = null;
            /*frame = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            frame.eraseColor(0xff000000);*/
        }

        //drawOverlay(frame, R.mipmap.playbtn_received, 0.17f);
        return frame;
    }

    public static Uri getUriForFile(Context context, File file) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            try {

                String packageId = context.getPackageName();
                return FileProvider.getUriForFile(context, packageId + FILE_PROVIDER, file);
            }
            catch (IllegalArgumentException e) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    throw new SecurityException();
                }
                else {

                    return Uri.fromFile(file);
                }
            }
        }
        else {

            return Uri.fromFile(file);
        }
    }

    public String getInternalPath(String type, String fileNameWithExtension) {
        return mContext.getFilesDir().getAbsolutePath() + "/" + type + "/" + fileNameWithExtension;
    }

    public Uri getInternalPathUri(String type, String fileNameWithExtension) {
        return Uri.parse("file:" + getInternalPath(type, fileNameWithExtension));
    }

    public Bitmap cropCenterSquareFromFile(String image, int size)  {

        FileInputStream fis = null;

        try {

            File file = new File(image);
            if (!file.exists()) {
                return null;
            }
            else {

                int sampleSize = calcSampleSize(file, size);

                if(sampleSize > 5) {
                    sampleSize = 16;
                }

                Log.e(TAG, "Sample Size - " + sampleSize);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = sampleSize;

                fis = new FileInputStream(file);
                Bitmap input = BitmapFactory.decodeStream(fis, null, options);

                if (input == null) {

                    return null;
                }
                else {

                    input = rotate(input, getRotation(file));
                    return cropCenterSquare(input, size);
                }
            }
        }
        catch (SecurityException e) {

            Log.d("avatar image.", "avatar image. SecurityException e" + image);
            e.printStackTrace();
            return null; // happens for example on Android 6.0 if contacts permissions get revoked
        }
        catch (FileNotFoundException e) {

            Log.d("avatar image.", "avatar image. FileNotFoundException e" + image);
            e.printStackTrace();
            return null;
        }
        finally {
            close(fis);
        }
    }

    public Bitmap cropCenterSquare(Uri image, int size) {

        if (image == null) {
            return null;
        }

        InputStream is = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calcSampleSize(image, size);
            is = mContext.getContentResolver().openInputStream(image);
            if (is == null) {
                return null;
            }


            Bitmap input = BitmapFactory.decodeStream(is, null, options);
            if (input == null) {
                return null;
            } else {

                input = rotate(input, getRotation(image));
                return cropCenterSquare(input, size);
            }
        } catch (SecurityException e) {
            Log.d("avatar image.", "avatar image. SecurityException e" + image);
            e.printStackTrace();
            return null; // happens for example on Android 6.0 if contacts permissions get revoked
        } catch (FileNotFoundException e) {
            Log.d("avatar image.", "avatar image. FileNotFoundException e" + image);
            e.printStackTrace();
            return null;
        } finally {
            close(is);
        }
    }

    public Bitmap cropCenter(Uri image, int newHeight, int newWidth) {
        if (image == null) {
            return null;
        }
        InputStream is = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calcSampleSize(image, Math.max(newHeight, newWidth));
            is = mContext.getContentResolver().openInputStream(image);
            if (is == null) {
                return null;
            }
            Bitmap source = BitmapFactory.decodeStream(is, null, options);
            if (source == null) {
                return null;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            float xScale = (float) newWidth / sourceWidth;
            float yScale = (float) newHeight / sourceHeight;
            float scale = Math.max(xScale, yScale);
            float scaledWidth = scale * sourceWidth;
            float scaledHeight = scale * sourceHeight;
            float left = (newWidth - scaledWidth) / 2;
            float top = (newHeight - scaledHeight) / 2;

            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
            Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(dest);
            canvas.drawBitmap(source, null, targetRect, null);
            if (source.isRecycled()) {
                source.recycle();
            }
            return dest;
        } catch (SecurityException e) {
            return null; //android 6.0 with revoked permissions for example
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            close(is);
        }
    }

    public Bitmap cropCenterSquare(Bitmap input, int size) {

        int w = input.getWidth();
        int h = input.getHeight();

        float scale = Math.max((float) size / h, (float) size / w);

        float outWidth = scale * w;
        float outHeight = scale * h;
        float left = (size - outWidth) / 2;
        float top = (size - outHeight) / 2;
        RectF target = new RectF(left, top, left + outWidth, top + outHeight);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(input, null, target, null);

        if (input != null && !input.isRecycled()) {
            input.recycle();
        }

        return output;
    }

    private int calcSampleSize(Uri image, int size) throws FileNotFoundException, SecurityException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(image), null, options);
        return calcSampleSize(options, size);
    }

    public static int calcSampleSize(File image, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getAbsolutePath(), options);
        return calcSampleSize(options, size);
    }

    public static int calcSampleSize(BitmapFactory.Options options, int size) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > size || width > size) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > size
                    && (halfWidth / inSampleSize) > size) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Dimensions getImageDimensions(File file) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int rotation = getRotation(file);
        boolean rotated = rotation == 90 || rotation == 270;
        int imageHeight = rotated ? options.outWidth : options.outHeight;
        int imageWidth = rotated ? options.outHeight : options.outWidth;
        return new Dimensions(imageHeight, imageWidth);
    }

    public Dimensions getVideoDimensions(File file) throws NotAVideoFile {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            metadataRetriever.setDataSource(file.getAbsolutePath());
        } catch (Exception e) {
            throw new NotAVideoFile();
        }
        String hasVideo = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
        if (hasVideo == null) {
            throw new NotAVideoFile();
        }
        int rotation = extractRotationFromMediaRetriever(metadataRetriever);
        boolean rotated = rotation == 90 || rotation == 270;
        int height;
        try {
            String h = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            height = Integer.parseInt(h);
        } catch (Exception e) {
            height = -1;
        }
        int width;
        try {
            String w = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            width = Integer.parseInt(w);
        } catch (Exception e) {
            width = -1;
        }
        metadataRetriever.release();
        Log.d(TAG, "extracted video dims " + width + "x" + height);
        return rotated ? new Dimensions(width, height) : new Dimensions(height, width);
    }

    private int extractRotationFromMediaRetriever(MediaMetadataRetriever metadataRetriever) {
        int rotation;
        if (Build.VERSION.SDK_INT >= 17) {
            String r = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            try {
                rotation = Integer.parseInt(r);
            } catch (Exception e) {
                rotation = 0;
            }
        } else {
            rotation = 0;
        }
        return rotation;
    }

    public class Dimensions {
        public final int width;
        public final int height;

        public Dimensions(int height, int width) {
            this.width = width;
            this.height = height;
        }
    }

    private class NotAVideoFile extends Exception {

    }

    public class FileCopyException extends Exception {
        private static final long serialVersionUID = -1010013599132881427L;
        private final int resId;

        public FileCopyException(int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return resId;
        }
    }

    public Bitmap getProfileImage(String profileImage, int size) {
        if (profileImage == null) {
            return null;
        }
        Bitmap bm = cropCenter(getInternalPathUri(FOLDER_PROFILE, profileImage), size, size);
        return bm;
    }

    public static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    public static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean weOwnFile(Context context, Uri uri) {
        if (uri == null || !ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return false;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return fileIsInFilesDir(context, uri);
        } else {
            return weOwnFileLollipop(uri);
        }
    }

    /**
     * This is more than hacky but probably way better than doing nothing
     * Further 'optimizations' might contain to get the parents of CacheDir and NoBackupDir
     * and check against those as well
     */
    private static boolean fileIsInFilesDir(Context context, Uri uri) {
        try {
            final String haystack = context.getFilesDir().getParentFile().getCanonicalPath();
            final String needle = new File(uri.getPath()).getCanonicalPath();
            return needle.startsWith(haystack);
        } catch (IOException e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean weOwnFileLollipop(Uri uri) {
        try {
            File file = new File(uri.getPath());
            FileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).getFileDescriptor();
            StructStat st = Os.fstat(fd);
            return st.st_uid == android.os.Process.myUid();
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
