package com.dolabs.emircom.utils;

import static android.os.Build.UNKNOWN;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.dolabs.emircom.R;
import com.dolabs.emircom.handlers.AppInterface;
import com.dolabs.emircom.session.SessionContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import dev.b3nedikt.app_locale.AppLocale;

public class Utility {



    private Utility() {

    }



    public static String generateDeviceId() {

        String deviceId = "";

        try {

            deviceId = Build.SERIAL;

            if (deviceId == null || deviceId.isEmpty() || deviceId.equalsIgnoreCase(UNKNOWN)) {

                try {

                    deviceId = Settings.Secure.getString( SessionContext.mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (deviceId == null || deviceId.isEmpty()) {

                    deviceId = UUID.randomUUID().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deviceId;
    }

    public static String ordinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    public static boolean checkPermission(Activity activity) {

        try {

            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                return true;
            } else {

                Log.e("checkPermission", "Permission not granted, requesting");

                List<String> permissions = new ArrayList<String>();

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (permissions.size() > 0) {

                    ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), 1);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    /**
     * Store in Internal to hide from User
     * 13072017AH
     *
     * @param ctx
     * @return
     */
    public static String getInternalFolder(Context ctx) {
        String mediaLoc = "";
        try {
            mediaLoc = ctx.getPackageManager().getPackageInfo( SessionContext.mContext.getPackageName(), 0).applicationInfo.dataDir;

            mediaLoc = mediaLoc + "/contacts" + File.separator;
            Log.d("Internal Maan", "Internal Maan  " + mediaLoc);


            File imageDir = new File(mediaLoc);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
        } catch (Exception e) {
            mediaLoc = "";
        }
        return mediaLoc;
    }

    public static void setLocale(String localeStr) {

        Locale locale = new Locale(localeStr);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;

        SessionContext.mContext.getResources().updateConfiguration(config, SessionContext.mContext.getResources().getDisplayMetrics());

        SessionContext.getInstance().setIsArabic(localeStr.equalsIgnoreCase("ar"));
    }
    public static void setLocale() {

        Locale locale;

        if(SessionContext.getInstance().isArabic()) {

            locale = new Locale("ar");
        }
        else {

            locale = Locale.ENGLISH;
        }

        AppLocale.setDesiredLocale(locale);
    }

    public static void showKeyboard(Context context) {

        try {

            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View v = ((Activity) context).getCurrentFocus();
            if (v == null)
                return;

            inputManager.showSoftInputFromInputMethod(v.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static  AlertDialog dialog;
    public static void showAlertDialog(Context context, int ic_dialog_alert, String title, String message, String firstButton, String secondButton, final AppInterface appInterface)

        {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set the custom layout
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.custom_alert_layout, null);
            View cardRootView = customLayout.findViewById(R.id.feed_error_layout);

            TextView tvFirstButton = cardRootView.findViewById(R.id.retry_tv);
            TextView tvSecondButton = cardRootView.findViewById(R.id.retry_tv_no);
            ImageView errorImage = cardRootView.findViewById(R.id.title_image);
            TextView errorTitle = cardRootView.findViewById(R.id.title_tv);
            TextView errorMessage = cardRootView.findViewById(R.id.message_tv);


            if (firstButton != null) {
                tvFirstButton.setVisibility(View.VISIBLE);
                tvFirstButton.setText(firstButton);
            } else {
                tvFirstButton.setVisibility(View.GONE);
            }


            if (secondButton != null) {
                tvSecondButton.setVisibility(View.VISIBLE);
                tvSecondButton.setText(secondButton);
            } else {
                tvSecondButton.setVisibility(View.GONE);
            }


            View finalCardRootView = cardRootView;
            tvFirstButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(dialog!=null)
                        dialog.dismiss();
                    if (appInterface != null)
                        appInterface.onCallback(1);

                }
            });
            tvSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(dialog!=null)
                        dialog.dismiss();
                    if (appInterface != null)
                        appInterface.onCallback(2);
                }
            });

            if (ic_dialog_alert > 0) {
                errorImage.setImageResource(ic_dialog_alert);
                errorImage.setVisibility(View.VISIBLE);
            }
            else {
                errorImage.setVisibility(View.GONE);
            }

            errorTitle.setText(title);
            errorMessage.setText(message);
        builder.setView(customLayout);

        // create and show the alert dialog
             dialog = builder.create();
        dialog.show();

    }

    public static void showAlert(View rootView, int ic_dialog_alert, String title, String message, String firstButton, String secondButton, final AppInterface appInterface) {

        try {
            View cardRootView = rootView.findViewById(R.id.feed_error_layout);
            if(cardRootView==null)
                cardRootView = LayoutInflater.from(rootView.getContext()).inflate(
                        R.layout.alert_no_feed, null);


            TextView tvFirstButton = cardRootView.findViewById(R.id.retry_tv);
            TextView tvSecondButton = cardRootView.findViewById(R.id.retry_tv_no);
            ImageView errorImage = cardRootView.findViewById(R.id.title_image);
            TextView errorTitle = cardRootView.findViewById(R.id.title_tv);
            TextView errorMessage = cardRootView.findViewById(R.id.message_tv);


            if (firstButton != null) {
                tvFirstButton.setVisibility(View.VISIBLE);
                tvFirstButton.setText(firstButton);
            } else {
                tvFirstButton.setVisibility(View.GONE);
            }


            if (secondButton != null) {
                tvSecondButton.setVisibility(View.VISIBLE);
                tvSecondButton.setText(secondButton);
            } else {
                tvSecondButton.setVisibility(View.GONE);
            }


            View finalCardRootView = cardRootView;
            tvFirstButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    finalCardRootView.setVisibility(View.GONE);
                    if (appInterface != null)
                        appInterface.onCallback(1);

                }
            });
            tvSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    finalCardRootView.setVisibility(View.GONE);
                    if (appInterface != null)
                        appInterface.onCallback(2);
                }
            });
            cardRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalCardRootView.setVisibility(View.GONE);
                }
            });

            if (ic_dialog_alert > 0) {
                errorImage.setImageResource(ic_dialog_alert);
                errorImage.setVisibility(View.VISIBLE);
            }
            else {
                errorImage.setVisibility(View.GONE);
            }

            errorTitle.setText(title);
            errorMessage.setText(message);


            animateErrorCardView(cardRootView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void showAlert(Activity act, int ic_dialog_alert, String title, String message, String firstButton, String secondButton, final AppInterface appInterface) {

        final View cardRootView = act.findViewById(R.id.feed_error_layout);

        TextView tvFirstButton = act.findViewById(R.id.retry_tv);
        TextView tvSecondButton = act.findViewById(R.id.retry_tv_no);
        ImageView errorImage = act.findViewById(R.id.title_image);
        TextView errorTitle = act.findViewById(R.id.title_tv);
        TextView errorMessage = act.findViewById(R.id.message_tv);


        if (firstButton != null) {
            tvFirstButton.setVisibility(View.VISIBLE);
            tvFirstButton.setText(firstButton);
        } else {
            tvFirstButton.setVisibility(View.GONE);
        }


        if (secondButton != null) {
            tvSecondButton.setVisibility(View.VISIBLE);
            tvSecondButton.setText(secondButton);
        } else {
            tvSecondButton.setVisibility(View.GONE);
        }


        tvFirstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                cardRootView.setVisibility(View.GONE);
                if (appInterface != null)
                    appInterface.onCallback(1);

            }
        });
        tvSecondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cardRootView.setVisibility(View.GONE);
                if (appInterface != null)
                    appInterface.onCallback(2);
            }
        });

        if (ic_dialog_alert > 0) {
            errorImage.setImageResource(ic_dialog_alert);
            errorImage.setVisibility(View.VISIBLE);
        }
        else {
            errorImage.setVisibility(View.GONE);
        }

        errorTitle.setText(title);
        errorMessage.setText(message);


        animateErrorCardView(cardRootView);
    }

    private static void animateErrorCardView(View cardRootView) {


        final View cardCardView = cardRootView.findViewById(R.id.error_card_view);
        cardRootView.setVisibility(View.VISIBLE);

        final Animation myAnim = AnimationUtils.loadAnimation(cardRootView.getContext(), R.anim.bounce);
        double animationDuration = 500;
        myAnim.setDuration((long) animationDuration);
       /* BounceInterpolator interpolator = new BounceInterpolator(0.25, 7);
        myAnim.setInterpolator(interpolator);
        //myAnim.setRepeatCount(2);
        cardCardView.startAnimation(myAnim);*/
    }
    public static void hideKeyboard(Context context) {

        try {

            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View v = ((Activity) context).getCurrentFocus();
            if (v == null)
                return;

            inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void hideKeyboard(View  rootView) {

        try {

            InputMethodManager inputManager = (InputMethodManager) rootView
                    .getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            IBinder binder = rootView.getWindowToken();
            inputManager.hideSoftInputFromWindow(binder,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Changes the System Bar Theme. */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static final void setSystemBarTheme(final Activity pActivity, final boolean pIsDark) {
        // Fetch the current flags.
        final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        pActivity.getWindow().getDecorView().setSystemUiVisibility(pIsDark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
    }

    public static  boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) SessionContext.mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    public static int convertDpToPixel(float dp, Resources resources) {


        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getActionBarHeight(Activity context) {

        int actionBarHeight = 70;

        TypedValue tv = new TypedValue();

        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        else {

            actionBarHeight = convertDpToPixel(70, context.getResources());
        }

        return actionBarHeight;
    }


    public static String ValidString(String phone_number) {
        if(phone_number ==null || phone_number.isEmpty())
            return "--";
        else
            return phone_number;
    }
}