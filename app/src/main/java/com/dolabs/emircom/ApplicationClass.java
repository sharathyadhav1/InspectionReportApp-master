package com.dolabs.emircom;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
import com.dolabs.emircom.activity.AppActivity;

import com.dolabs.emircom.connector.AppOkHttpClient;
import com.dolabs.emircom.prefs.GlobalPreferManager;
import com.dolabs.emircom.session.SessionContext;
import com.dolabs.emircom.utils.Utility;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ApplicationClass extends MultiDexApplication {

    private final String TAG = this.getClass().getSimpleName();
    private static Context mContext;

    public static boolean IsAppInForeground = false;

    private final List<AppActivity> currentActivity = new ArrayList<>();
    private View currentRootView;

    @Override
    public void onCreate() {

        super.onCreate();

        try {

            mContext = this;
            SessionContext.mContext = mContext;

           /* AppLocale.setSupportedLocales(new ArrayList<>(Arrays.asList(Locale.ENGLISH, new Locale("ar"))));
            AppLocale.setAppLocaleRepository(new SharedPrefsAppLocaleRepository(mContext));*/


            GlobalPreferManager.initializePreferenceManager(getApplicationContext());

            AndroidNetworking.initialize(getAppContext());
            AndroidNetworking.enableLogging();
            Utility.setLocale();

            InitializeDatabase();

            registerForAppStates();

            final Picasso picasso = new Picasso.Builder(mContext)
                    .downloader(new OkHttp3Downloader(AppOkHttpClient.GetOkHttpClient()))
                    .build();

            Picasso.setSingletonInstance(picasso);

            //FacebookSdk.sdkInitialize(getApplicationContext());
            //Places.initialize(getApplicationContext(), "AIzaSyDbwm6-AomA_8vXI0A-BdXYA8EjEBfKU_A");
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onTrimMemory(int level) {

        super.onTrimMemory(level);

        Log.e(TAG, "onTrimMemory");

        freeUpMemory();
    }

    @Override
    public void onLowMemory() {

        Log.e(TAG, "onLowMemory");
        freeUpMemory();
        super.onLowMemory();
    }

    public void freeUpMemory() {

        try {

            Log.w(TAG, "Freeing up memory!");
            System.gc();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public AppActivity getCurrentActivity() {
        return currentActivity.isEmpty() ? null : currentActivity.get(currentActivity.size() - 1);
    }
    public void addToActivityStack(AppActivity currentActivity) {
        this.currentActivity.add(currentActivity);
    }

    public void removeFromActivityStack(AppActivity currentActivity) {
        this.currentActivity.remove(currentActivity);
    }


    public void setCurrentRootView(View rootView) {
        this.currentRootView = rootView;
    }

    public View getCurrentRootView() {
        return currentRootView;
    }

    public static Context getAppContext() {
        return ApplicationClass.mContext;
    }

    public static String getAppName() {
        return ApplicationClass.mContext.getString(R.string.app_name);
    }

    private void registerForAppStates() {

        try {

            ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AppLifecycleListener implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onAppDidEnterForeground() {

            Log.d("onAppDidEnterForeground","onAppDidEnterForeground");

            IsAppInForeground = true;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onAppDidEnterBackground() {

            Log.d("onAppDidEnterForeground","onAppDidEnterBackground");

            IsAppInForeground = false;
        }
    }

    public static void InitializeDatabase() {

        try {

            FlowManager.init(mContext);

            //region init encrypted database
            /*FlowManager.init(new FlowConfig.Builder(mContext)
                    .addDatabaseConfig(new DatabaseConfig.Builder(AppDatabase.class)
                            .openHelper(new DatabaseConfig.OpenHelperCreator() {

                                @Override
                                public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {

                                    return new SQLCipherHelperImpl(databaseDefinition, helperListener);
                                }
                            })
                            .build())
                    .build());*/
            //endregion
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
}
