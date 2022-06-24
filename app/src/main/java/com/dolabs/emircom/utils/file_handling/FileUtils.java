package com.dolabs.emircom.utils.file_handling;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.util.Pair;


import com.dolabs.emircom.R;
import com.dolabs.emircom.session.SessionContext;
import com.dolabs.emircom.utils.MimeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileUtils {

	private static final String TAG = FileUtils.class.getClass().getSimpleName();

	//App Folders
	public static final String FOLDER_PROFILE = "Profile";
	public static final String FOLDER_DOCUMENTS = "Documents";
	public static final String FOLDER_OTHERS = "Others";
	public static final String FOLDER_CACHE = "Cache";
	public static final String PREFIX_TEMP = ".temp";

	public enum LOG_FILE_TYPE {

		FILE_XMPP(1),
		FILE_SYSTEM(2),
		FILE_OTHER(3),
		FILE_WEBRTC(4);

		private final int value;

		LOG_FILE_TYPE(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		private static final Map<Integer, LOG_FILE_TYPE> lookup = new HashMap<>();

		static {

			for (LOG_FILE_TYPE s : EnumSet.allOf(LOG_FILE_TYPE.class))
				lookup.put(s.getValue(), s);
		}

		public static LOG_FILE_TYPE get(int code) {
			return lookup.get(code);
		}
	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri     The Uri to query.
	 * @author paulburke
	 */
	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {
		if (uri == null) {
			return null;
		}

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			String path = getDataColumn(context, uri, null, null);
			if (path != null) {
				File file = new File(path);
				if (!file.canRead()) {
					return null;
				}
			}
			return path;
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return uri.toString();
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} catch(Exception e) {
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static void DeleteAppFolder() {

		new Thread(()-> {

			DeleteAllFilesOfDir(new File(FileUtils.getAppRootFolder()));
		}).start();
	}

	public static void DeleteFolderWithFiles(File directory) {

		try {

			DeleteAllFilesOfDir(directory);
			directory.delete();
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static void DeleteAllFilesOfDir(File directory) {

		try {

			Log.d(TAG, "Directory: " + directory.getAbsolutePath() + "\n");

			final File[] files = directory.listFiles();

			if (files != null ) {

				for (File file : files) {

					if (file != null) {

						if (file.isDirectory()) {

							DeleteAllFilesOfDir(file);
							file.delete();
						}
						else {

							Log.d(TAG, "File: " + file.getAbsolutePath() + "\n");
							file.delete();
						}
					}
				}
			}
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static String convertStreamToString(InputStream is) throws Exception {

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;

		try {

			reader = new BufferedReader(new InputStreamReader(is));
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		finally {

			if(reader != null)
				reader.close();
		}

		return sb.toString();
	}

	public static String getStringFromFile(String filePath) {

		String ret = null;

		try {

			File fl = new File(filePath);
			ret = getStringFromFile(fl);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static String getStringFromFile(File file) {

		String ret = null;
		FileInputStream fin = null;

		try {

			fin = new FileInputStream(file);
			ret = convertStreamToString(fin);
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		finally {

			try {

				if(fin != null)
					fin.close();
			}
			catch (Exception e) {}
		}

		return ret;
	}

	public static File getProfileDocFile(String fileName) {
		return getFile(FOLDER_PROFILE, fileName);
	}

	public static File getDocumentFile(String fileName) {
		return getFile(FOLDER_DOCUMENTS, fileName);
	}

	public static File getFile(String folder, String fileName) {
		return new File(getAppDirectory(folder), fileName);
	}

	public static String getAppDirectory(String type) {

		if(type == null || type.trim().isEmpty())
			type = FOLDER_OTHERS;

		/*Context context = ApplicationClass.getAppContext();
		type = context.getString(R.string.app_name) + " " + type;*/

		String rootFolder = getAppRootFolder();

		rootFolder = (rootFolder /*+ "/"+ context.getString(R.string.app_name)*/ + "/Media/" + type + "/");

		return rootFolder;
	}

	public static String GetCacheFolder() {

		File cacheFile = new File(getAppDirectory(FOLDER_CACHE));

		if(!cacheFile.exists()) {
			cacheFile.mkdirs();
		}

		return cacheFile.getAbsolutePath();
	}

	public static String getAppRootFolder() {

		Context context = SessionContext.mContext;
		boolean extAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		File rootFolder = extAvailable ? context.getExternalFilesDir(null) : context.getFilesDir();
		return rootFolder.getAbsolutePath();
	}

	public static File getPictureFolder(String fileName) {
		return new File(getPictureFolder(), fileName);
	}
	public static String getPictureFolder() {

		Context context = SessionContext.mContext;


		boolean extAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(extAvailable)
		{
			File pictureFolder = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"");
			return pictureFolder.getAbsolutePath();
		}
		else
		{
			File rootFolder = extAvailable ? context.getExternalFilesDir(null) : context.getFilesDir();
			return rootFolder.getAbsolutePath();
		}

	}

	public static File RenameFile(File file, String newName) {

		File destFile = file;

		try {

			String initialFilePath = file.getParentFile().getAbsolutePath();
			String destFilePath = initialFilePath + "/" + newName;

			//Log.e(TAG, "initialFilePath - " + initialFilePath);
			//Log.e(TAG, "destFilePath - " + destFilePath);

			destFile = new File(destFilePath);

			file.renameTo(destFile);

			if(destFile.exists()) {

				Log.d(TAG, "rename success");
			}
			else {

				Log.d(TAG, "rename failed");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return destFile;
	}

	public static File RenameToTempFile(File file) {

		File destFile = file;

		try {

			String initialFilePath = file.getAbsolutePath();
			String destFilePath = initialFilePath + PREFIX_TEMP;

			//Log.e(TAG, "initialFilePath - " + initialFilePath);
			//Log.e(TAG, "destFilePath - " + destFilePath);

			destFile = new File(destFilePath);

			file.renameTo(destFile);

			if(destFile.exists()) {

				Log.d(TAG, "rename success");
			}
			else {

				Log.d(TAG, "rename failed");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return destFile;
	}

	public static boolean CopyFile(File originalFile, File file) {

		Log.d(TAG, "copy file (" + file.getName() + ") to " + file.getName());

		boolean copySuccess = false;
		OutputStream os = null;
		InputStream is = null;

		try {

			if(!file.exists()) {

				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			is = new FileInputStream(originalFile);
			os = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}

			os.flush();
			copySuccess = true;
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		finally {

			FileBackend.close(os);
			FileBackend.close(is);
		}

		return copySuccess;
	}

	public static File MoveToFolder(File file, File destinationFolder) {

		File destFile = file;

		try {

			String initialFilePath = file.getAbsolutePath();
			String destFilePath = destinationFolder.getAbsolutePath() + "/" + file.getName();

			//Log.e(TAG, "initialFilePath - " + initialFilePath);
			//Log.e(TAG, "destFilePath - " + destFilePath);

			destFile = new File(destFilePath);
			destFile.getParentFile().mkdirs();

			boolean isMoveSuccess = file.renameTo(destFile);

			if(isMoveSuccess) {

				Log.d(TAG, "rename success");
				file.delete();
				return destFile;
			}
			else {

				Log.d(TAG, "rename failed");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}

	public static File MoveToFolder(String filePath, String destinationFolderPath) {
		return MoveToFolder(new File(filePath), new File(destinationFolderPath));
	}

	public static void CreateNewFile(File file) throws Exception {

		if(!file.exists()) {

			CreateFolders(file.getParentFile());
			boolean fileCreated = file.createNewFile();
			//Log.e(TAG, "File Created - " + fileCreated);
		}
	}

	public static void CreateFolders(File folder) {

		if (!folder.exists()) {
			boolean folderCreated = folder.mkdirs();
			//Log.e(TAG, "Folder Created - " + folderCreated);
		}
	}

	public static Pair<Integer, Integer> GetIMGDimension(File file) {
		return GetIMGDimension(file.getAbsolutePath());
	}

	public static Pair<Integer, Integer> GetIMGDimension(String absPath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(absPath, options);
		return new Pair<>(options.outWidth, options.outHeight);
	}

	public static void OpenImageInGallery(String absPath) {
		OpenImageInGallery(new File(absPath));
	}

	public static void OpenImageInGallery(File file) {

		Context context = SessionContext.mContext;

		try {

			if(!file.exists()) {
				return;
			}

			Uri fileUri = FileBackend.getUriForFile(context, file);
			Intent openIntent = new Intent(Intent.ACTION_VIEW);
			String mime = MimeUtils.guessMimeTypeFromUri(fileUri);
			openIntent.setDataAndType(fileUri, mime);
			openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
			PackageManager manager = SessionContext.mContext.getPackageManager();

			List<ResolveInfo> info = manager.queryIntentActivities(openIntent, 0);
			if (info.size() == 0) {
				openIntent.setDataAndType(fileUri, "*/*");
			}

			context.startActivity(openIntent);
		}
		catch (Exception e) {

			e.printStackTrace();
			Toast.makeText(context, R.string.error_while_processing, Toast.LENGTH_SHORT).show();
		}
	}
}