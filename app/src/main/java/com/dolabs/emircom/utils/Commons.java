package com.dolabs.emircom.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;


import com.androidadvance.topsnackbar.TSnackbar;
import com.dolabs.emircom.R;
import com.dolabs.emircom.connector.ConnectorTransfer;
import com.dolabs.emircom.connector.HTTPActionType;
import com.dolabs.emircom.connector.HTTPConnector;
import com.dolabs.emircom.connector.ServiceResponse;
import com.dolabs.emircom.prefs.GlobalPreferManager;
import com.dolabs.emircom.session.SessionContext;

import java.util.LinkedHashMap;
import java.util.Random;

public class Commons {

    private static final String TAG = Commons.class.getSimpleName();

    public static void showSnackbar(Activity activity, String message, int color, int drawable) {
        try {
            TSnackbar tSnackbar = TSnackbar.make(activity.findViewById(android.R.id.content), message, TSnackbar.LENGTH_SHORT);
            View snackbarView = tSnackbar.getView();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                snackbarView.setElevation(0);
            }
            snackbarView.setPadding(0,Utility.convertDpToPixel(25f,activity.getResources()),0,0);
            TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.getResources().getDimension(R.dimen.font_snack_bar));
            textView.setTextColor(Color.WHITE);
            snackbarView.setBackgroundColor(color);
            tSnackbar.setIconLeft(drawable, 36);
            tSnackbar.setIconPadding(16);
            tSnackbar.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void showSnackbar(View view, String message, int color, int drawable) {
        try {
            TSnackbar tSnackbar = TSnackbar.make(view, message, TSnackbar.LENGTH_SHORT);
            View snackbarView = tSnackbar.getView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                snackbarView.setElevation(0);
            }
            snackbarView.setPadding(0,Utility.convertDpToPixel(25f,view.getResources()),0,0);
            TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getResources().getDimension(R.dimen.font_snack_bar));
            textView.setTextColor(Color.WHITE);
            snackbarView.setBackgroundColor(color);
            tSnackbar.setIconLeft(drawable, 36);
            tSnackbar.setIconPadding(16);
            tSnackbar.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String getTemperoryId() {

        return "TEMP_"+generateRandom(12);
    }
    public static long generateRandom(int length) {
        Random random = new Random();
        char[] digits = new char[length];
        digits[0] = (char) (random.nextInt(9) + '1');
        for (int i = 1; i < length; i++) {
            digits[i] = (char) (random.nextInt(10) + '0');
        }
        return Long.parseLong(new String(digits));
    }

    public static String getFolderId() {
        String random_id = String.format("%04d", new Random().nextInt(10000));
        int  folderIndex= GlobalPreferManager.getInt(Constants.PREF_FOLDER_INDEX,0)+1;
        GlobalPreferManager.setInt(Constants.PREF_FOLDER_INDEX,folderIndex);
        String app_folderIndex =folderIndex+"adr"+random_id;
        return app_folderIndex;

    }

    public static String getListId() {
        String random_id = String.format("%04d", new Random().nextInt(10000));
        int  listIndex= GlobalPreferManager.getInt(Constants.PREF_LIST_INDEX,0)+1;
        GlobalPreferManager.setInt(Constants.PREF_LIST_INDEX,listIndex);
        String app_listIndex = listIndex+"adr"+random_id;
        return app_listIndex;

    }

    public static String getTaskId() {
        String random_id = String.format("%04d", new Random().nextInt(10000));
        int  taskindex= GlobalPreferManager.getInt(Constants.PREF_TASK_INDEX,0)+1;
        GlobalPreferManager.setInt(Constants.PREF_TASK_INDEX,taskindex);
        String app_taskIndex = taskindex+"adr"+random_id;
        return app_taskIndex;

    }

    public static String getTaskMediaId() {
        String random_id = String.format("%04d", new Random().nextInt(10000));
        int  mediaindex= GlobalPreferManager.getInt(Constants.PREF_MEDIA_INDEX,0)+1;
        GlobalPreferManager.setInt(Constants.PREF_MEDIA_INDEX,mediaindex);
        String app_taskIndex = mediaindex+"adr"+random_id;
        return app_taskIndex;

    }

    public static String getPreferenceId(String pref_key) {
        String random_id = String.format("%04d", new Random().nextInt(10000));
        int  index= GlobalPreferManager.getInt(pref_key,0)+1;
        GlobalPreferManager.setInt(pref_key,index);
        String app_Index = index+"adr"+random_id;
        return app_Index;

    }




    public static void refreshToken(Context context, ServiceResponse serviceResponse){

        try {

            ConnectorTransfer connectorTransfer = new ConnectorTransfer();
            connectorTransfer.setActionType(HTTPActionType.POST);
            connectorTransfer.setRequestUrl(Constants.URL_TOKEN);

            LinkedHashMap<String, Object> reqBodyMap = new LinkedHashMap<>();



            reqBodyMap.put("token", SessionContext.getInstance().getAccessToken());
            reqBodyMap.put("refresh_token", SessionContext.getInstance().getRefreshToken());
            reqBodyMap.put("username", SessionContext.getInstance().getPhoneNumber());

            connectorTransfer.setRequestBodyMap(reqBodyMap);

            HTTPConnector httpConnector = new HTTPConnector(context, connectorTransfer, serviceResponse);

            httpConnector.execute();
        }
        catch (Exception e) {

            e.printStackTrace();

            if (serviceResponse != null)
                serviceResponse.onError(00, "CSeX", "");

            // mActivity.showToast(R.string.error_while_processing);
        }

    }






















}
