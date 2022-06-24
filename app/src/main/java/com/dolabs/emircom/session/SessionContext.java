package com.dolabs.emircom.session;


import static com.dolabs.emircom.utils.Constants.THEME_WHITE;
import static com.dolabs.emircom.utils.Constants.TOKEN_BEARER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.dolabs.emircom.utils.Utility;
import com.raizlabs.android.dbflow.sql.language.SQLite;

@SuppressLint("applyPrefEdits")
public class SessionContext {

    public static Context mContext;
    private static SessionContext sessionContext;
    Location location;
    private final String TAG = this.getClass().getSimpleName();
    
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private boolean istokenrefreshhhpo = false;
    
    private Boolean isArabic;
    private Boolean isNotificationChanged =false;
    private Boolean isCreated;
    private String accessToken;
    private String refreshToken;
    private String fcmToken;

    private String deviceId;
    private String deviceName = null;

    private boolean deviceRegistered;
    private String phoneNumber = null;
    private String countryCode = null;
    private String mobileNumber = null;




    private boolean isAutoArcheive;
    private Uri notificationUri;

    private SessionContext() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPreferences.edit();
    }

    public static SessionContext getInstance() {

        if (sessionContext == null) {
            sessionContext = new SessionContext();
        }

        return sessionContext;
    }

    public Boolean getNotificationChanged() {
        return isNotificationChanged;
    }

    public void setNotificationChanged(Boolean notificationChanged) {
        isNotificationChanged = notificationChanged;
    }

    public boolean isArabic() {

        isArabic =sharedPreferences.getBoolean("isArabic", false);
        return isArabic;
    }

    public void setIsArabic(boolean isArabic) {

        editor.putBoolean("isArabic", isArabic);
        editor.apply();

        this.isArabic = isArabic;
    }

    public boolean isCreated() {

        isCreated =sharedPreferences.getBoolean("isCreated", false);
        return isCreated;
    }

    public void setIsCreated(boolean isCreated) {

        editor.putBoolean("isCreated", isCreated);
        editor.apply();

        this.isCreated = isCreated;
    }

    public boolean isAutoArcheive() {
        isAutoArcheive =sharedPreferences.getBoolean("isAutoArcheive", false);
        return isAutoArcheive;
    }

    public void setAutoArcheive(boolean autoArcheive) {
        editor.putBoolean("isAutoArcheive", autoArcheive);
        editor.apply();
        isAutoArcheive = autoArcheive;
    }

    public Uri getNotificationUri() {
        notificationUri = Uri.parse(sharedPreferences.getString("notificationURI", Settings.System.DEFAULT_NOTIFICATION_URI.toString() ));
        return notificationUri;
    }

    public void setNotificationUri(Uri notificationUri) {
        editor.putString("notificationURI", notificationUri.toString());
        editor.apply();

        this.notificationUri = notificationUri;
    }

    public String getAccessToken() {

        if(accessToken == null) {
            accessToken = sharedPreferences.getString("accessToken", null);
        }

        return accessToken;
    }

    public void setAccessToken(String accessToken) {

        editor.putString("accessToken", accessToken);
        editor.apply();
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {

        if(refreshToken==null)
            refreshToken = sharedPreferences.getString("refreshToken", null);
        return refreshToken;

    }

    public void setRefreshToken(String refreshToken) {
        editor.putString("refreshToken", refreshToken);
        editor.apply();
        this.refreshToken = refreshToken;


    }

    public String getDeviceId() {

        if (deviceId == null || deviceId.isEmpty()) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            deviceId = sharedPreferences.getString("deviceId", null);

            if (deviceId == null || deviceId.isEmpty()) {

                deviceId = Utility.generateDeviceId();
                setDeviceId(deviceId);
            }
        }

        return deviceId;
    }

    public void setDeviceId(String deviceId) {

        if (deviceId != null) {

            editor.putString("deviceId", deviceId);
            editor.apply();
        }

        this.deviceId = deviceId;
    }

    public String getDeviceName() {

        try {

            if(deviceName == null || deviceName.trim().isEmpty()) {

                deviceName = Build.MODEL;

                if (deviceName == null || deviceName.trim().isEmpty()) {
                    deviceName = Build.DEVICE;
                }

                if (deviceName == null || deviceName.trim().isEmpty()) {
                    deviceName = Build.MANUFACTURER;
                }
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        if(deviceName == null || deviceName.trim().isEmpty()) {
            deviceName = "Android";
        }

        return deviceName;
    }

    public String getFcmToken() {

        fcmToken = sharedPreferences.getString("fcmToken", null);
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {

        editor.putString("fcmToken", fcmToken);
        editor.apply();
        this.fcmToken = fcmToken;
    }

    public void setDeviceRegistered(boolean deviceRegistered) {

        editor.putBoolean("deviceRegistered", deviceRegistered);
        editor.apply();

        this.deviceRegistered = deviceRegistered;
    }

    public boolean isDeviceRegistered() {

        deviceRegistered = sharedPreferences.getBoolean("deviceRegistered", false);
        return deviceRegistered;
    }

    public void clearSession(boolean isLogout) {

        if (isLogout)
            sessionContext = null;
    }

    public void setCountryCode(String countryCode) {

        if(countryCode != null) {
            editor.putString("countryCode", countryCode);
            editor.apply();
        }
        else editor.remove("countryCode");

        this.countryCode = countryCode;
    }
    public String getCountryCode() {

        if(countryCode == null) {
            countryCode = sharedPreferences.getString("countryCode", "");
        }

        return countryCode;
    }
    public void setMobileNumber(String mobileNumber) {

        if(mobileNumber != null) {
            editor.putString("mobileNumber", mobileNumber);
            editor.apply();
        }
        else editor.remove("mobileNumber");

        this.mobileNumber = mobileNumber;
    }
    public String getMobileNumber() {

        if(mobileNumber == null) {
            mobileNumber = sharedPreferences.getString("mobileNumber", "");
        }

        return mobileNumber;
    }

    public String getPhoneNumber() {

        if (phoneNumber == null) {
            phoneNumber = sharedPreferences.getString("phoneNumber", "");
        }

        return phoneNumber;
    }

    public void setPhoneNumber(String contactNumber) {
        if (contactNumber != null) {
            editor.putString("phoneNumber", contactNumber);
            editor.apply();
        } else editor.remove("phoneNumber");

        this.phoneNumber = contactNumber;
    }

    public String getBearerToken() {
        return TOKEN_BEARER;
    }

    public boolean isIstokenrefreshhhpo() {
        return istokenrefreshhhpo;
    }

    public void setIstokenrefreshhhpo(boolean istokenrefreshhhpo) {
        this.istokenrefreshhhpo = istokenrefreshhhpo;
    }

    public int getTheme() {
        return THEME_WHITE;
    }




}