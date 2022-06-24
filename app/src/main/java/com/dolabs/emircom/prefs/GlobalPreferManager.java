package com.dolabs.emircom.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GlobalPreferManager {

    public static SharedPreferences preferences;

    public static void saveArray(String tag, ArrayList<String> mArray) {
        JSONArray array = new JSONArray(mArray);
        String json = array.toString();
        Editor editor = preferences.edit();
        editor.putString(tag, json);
        editor.apply();
    }

    public static ArrayList<String> loadArray(String tag) {
        ArrayList<String> array = new ArrayList<String>();


        String json = preferences.getString(tag, "");

        try {

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                array.add(jsonArray.getString(i));

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return array;
    }

    public static void initializePreferenceManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public static void setString(String key, String value) {

        Log.e(key, value);

        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public static void setInt(String key, int value) {
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setFloat(String key, float value) {
        Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    public static class Keys {

        public static final String IS_LOGIN = "is_login";
        public static final String IS_RATING_FETCHED = "is_rating_fetched";
        public static final String CURRENT_LATITUDE = "latitude";
        public static final String CURRENT_LONGITUDE = "longitude";
        public static final String CURRENT_PLACE = "place_name";
        public static final String CURRENT_COUNTRY = "country_name";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_TITLE = "user_title";
        public static final String USER_NAME_BOOK = "user_name_book";
        public static final String USER_LAST_NAME = "user_last_name";
        public static final String USER_LAST_NAME_BOOK = "user_last_name_book";
        public static final String USER_EMAIL = "user_email";
        public static final String USER_TOKEN = "token";
        public static final String USER_EMAIL_BOOK = "user_email_book";
        public static final String USER_MOBILE = "mobile";
        public static final String USER_MOBILE_BOOK = "mobile_book";
        public static final String USER_STATUS = "status";
        public static final String USER_CITY = "city";
        public static final String USER_CITY_BOOK = "city_book";
        public static final String USER_COUNTRY = "country";
        public static final String USER_COUNTRY_BOOK= "country_book";
        public static final String USER_IMAGE = "image";

        public static final String SHOW_ON_BOARDING = "onBording";

        public static final String GEOPLACE_LOCATION_LATITUDE = "geoplace_location_latitude";
        public static final String GEOPLACE_LOCATION_LONGITUDE = "geoplace_location_longitude";
        public static final String GEOPLACE_LOCATION_NAME = "geoplace_location_name";
        public static final String GEOPLACE_LOCATION_ADDRESS = "geoplace_location_address";
        public static final String GEOPLACE_LOCATION_IMAGE = "geoplace_location_image";
        public static final String GEOPLACE_LOCATION_WEATHER = "geoplace_location_weather";


        public static final String RATING_JSON = "rating_json_string";
        public static final String FAVORITE_JSON = "favorite_json_string";

        public static final String IS_NOTIFICATION_READ = "all_notofication_read";


    }
}
