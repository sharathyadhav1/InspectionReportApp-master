package com.dolabs.emircom.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.dolabs.emircom.ApplicationClass;
import com.dolabs.emircom.R;
import com.dolabs.emircom.adapter.BannerListAdapter;
import com.dolabs.emircom.session.SessionContext;
import com.dolabs.emircom.utils.Utility;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.b3nedikt.app_locale.AppLocale;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class AppActivity extends AppCompatActivity  implements EasyPermissions.PermissionCallbacks  {

    protected final String TAG = this.getClass().getSimpleName();
    protected Context mContext;
    protected AppActivity mActivity;
    protected ApplicationClass mApplication;

    private boolean isArabic;

    protected View rootView;
    protected ProgressBar progressBar;
    protected Toast toast;

    public SessionContext sessionContext;

    protected boolean isActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.mContext = this;
        this.mActivity = this;
        this.mApplication = getApp();
        this.sessionContext = SessionContext.getInstance();

        isArabic = sessionContext.isArabic();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Utility.setLocale();
    }

    @Override
    public void setContentView(int layoutResID) {

        super.setContentView(layoutResID);
        setRootView();
    }

    @Override
    public void setContentView(View view) {

        super.setContentView(view);
        setRootView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {

        super.setContentView(view, params);
        setRootView();
    }

    @Override
    public void onTrimMemory(int level) {

        super.onTrimMemory(level);

        Log.e(TAG, "onTrimMemory");

        try {

            getApp().onTrimMemory(level);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {

        super.onLowMemory();

        Log.e(TAG, "onLowMemory");

        try {

            getApp().onLowMemory();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        setRootView();

        mApplication.setCurrentRootView(rootView);
        mApplication.addToActivityStack(this);

        isActive = true;

        if(isArabic != sessionContext.isArabic()) {

            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }


    @Override
    protected void onStop() {

        super.onStop();

        mApplication.removeFromActivityStack(this);
    }
    private void setRootView() {

        try {

            rootView = ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
            getApp().setCurrentRootView(rootView);

            progressBar = findViewById(R.id.progress_bar);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ApplicationClass getApp() {
        return (ApplicationClass) getApplication();
    }

    @Override @RequiresApi(api = Build.VERSION_CODES.M) @SuppressLint("NewApi")
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        try {

            boolean showAppSettings = false;

            for (String permission: perms) {

                showAppSettings = ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission);
                if(showAppSettings)
                    break;
            }

            if (showAppSettings /*EasyPermissions.somePermissionPermanentlyDenied(mActivity, perms)*/) {

                    new AppSettingsDialog.Builder(mActivity).build().show();

            }
            else {

                requestPermissions(perms.parallelStream().toArray(String[]::new), requestCode);
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocale.wrap(newBase));
    }

   /* @Override
    public Resources getResources() {
        return AppLocale.wrap(ApplicationClass.getAppContext()).getResources();
    }*/

    public void showBanner(String message) {
        showBanner(message, R.mipmap.ic_launcher_round);
    }

    public void showBannerSuccess(String message) {
        showBanner(message, R.mipmap.ic_launcher_round);
    }

    public void showBanner(int messageId) {
        showBanner(getString(messageId));
    }

    public void showBannerError(int messageId) {
        showBannerError(getString(messageId));
    }

    public void showBannerError(String message) {
        showBanner(message, R.drawable.error_icon);
    }

    public void showWaitOrNoInternetBanner() {

        boolean isConnected = Utility.isNetworkAvailable();
        showBannerError(isConnected ? R.string.please_wait : R.string.no_internet_error);
    }

    public void showBanner(String message, int icon) {
        showBanner(new ArrayList<>(Collections.singletonList(message)), icon);
    }

    public void showBanner(List<String> messages) {
        showBanner(messages, R.mipmap.ic_launcher_round);
    }

    public void showBannerError(List<String> messages) {
        showBanner(messages, R.drawable.error_icon);
    }

    public void showBanner(List<String> messages, int icon) {
        showBanner(null, messages, icon, false, null);
    }

    public void showBanner(List<String> messages, int icon, Intent intent) {
        showBanner(null, messages, icon, false, intent);
    }

    public void showBanner(String message, Intent intent) {
        showBanner(new ArrayList<>(Collections.singletonList(message)), R.mipmap.ic_launcher_round, intent);
    }

    public void showBanner(String title, List<String> messages, int icon, boolean showListForce, Intent intent) {

        try {

            hideBanner();

            if(messages.isEmpty()) {
                return;
            }

            if(title == null || title.trim().isEmpty()) {
                title = getString(R.string.app_name);
            }

            String finalTitle = title;
            boolean showListView = showListForce || messages.size() > 1;

            runOnUiThread(() -> {

                try {

                    CookieBar.build(mActivity)
                            .setTitle(finalTitle)
                            .setIcon(icon)
                            .setAction(R.string.dismiss, this::hideBanner)
                            .setTitleColor(R.color.colorPrimary)
                            .setMessage(showListView ? "" : messages.get(0))
                            .setTitleColor(R.color.md_grey_500)
                            .setMessageColor(R.color.black)
                            .setActionColor(R.color.black)
                            .setBackgroundColor(R.color.md_grey_300)
                            .setCustomView(R.layout.layout_banner_list)
                            .setEnableAutoDismiss(!showListView)
                            .setSwipeToDismiss(true)
                            .setCustomViewInitializer(rootView -> {

                                rootView.findViewById(R.id.tv_message).setVisibility(showListView ? View.GONE : View.VISIBLE);
                                rootView.findViewById(R.id.banner_list_view).setVisibility(showListView ? View.VISIBLE : View.GONE);

                                if(showListView) {

                                    BannerListAdapter bannerListAdapter = new BannerListAdapter(mContext, messages, objects -> {

                                    });

                                    RecyclerView bannerListView = rootView.findViewById(R.id.banner_list_view);
                                    bannerListView.setLayoutManager(new LinearLayoutManager(mContext));
                                    bannerListView.setAdapter(bannerListAdapter);
                                }

                                if(intent != null) {

                                    rootView.findViewById(R.id.cookie).setOnClickListener(v -> {

                                        startActivity(intent);
                                        //onInAppNotificationClicked(intent);
                                    });
                                }
                            })
                            .setDuration(5000)
                            .show();
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            });
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void hideBanner() {

        try {

            runOnUiThread(() -> {

                try {

                    CookieBar.dismiss(mActivity);
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            });
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void showToast(int id) {
        showToast(getString(id));
    }

    public void showToast(String error) {

        try {

            hideToast();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    toast = Toast.makeText(mContext, error, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });


        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void hideToast() {

        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (toast != null) {

                        toast.cancel();
                        toast = null;
                    }
                }
            });


        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void setProgressBar(ProgressBar progressBar) {

        if(progressBar != null) {
            this.progressBar = progressBar;
        }
    }

    public void showProgress(boolean state) {

        try {

            if(this.progressBar != null) {

                runOnUiThread(() -> {

                    this.progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
                });
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void showProgress(ProgressBar progressBar, boolean state) {

        try {

            if(progressBar != null) {

                runOnUiThread(() -> {

                    progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
                });
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public int getClr(int color) {
        return ContextCompat.getColor(mContext, color);
    }

    protected void onInAppNotificationClicked(Intent intent) {

    }
}
