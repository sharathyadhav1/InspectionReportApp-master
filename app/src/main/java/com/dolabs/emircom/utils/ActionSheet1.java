/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 baoyongzhang <baoyz94@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dolabs.emircom.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.dolabs.emircom.R;

import java.lang.reflect.Method;

/**
 * android-ActionSheet
 * Created by baoyz on 15/6/30.
 */
@SuppressWarnings("ResourceType")
public class ActionSheet1 extends Fragment implements View.OnClickListener {

    private static final String ARG_CANCEL_BUTTON_TITLE = "cancel_button_title";
    private static final String ARG_OTHER_BUTTON_TITLES = "other_button_titles";
    private static final String ARG_CANCELABLE_ONTOUCHOUTSIDE = "cancelable_ontouchoutside";
    private static final int CANCEL_BUTTON_ID = 100;
    private static final int BG_VIEW_ID = 10;
    private static final int TRANSLATE_DURATION = 200;
    private static final int ALPHA_DURATION = 300;

    private static final String EXTRA_DISMISSED = "extra_dismissed";

    private boolean mDismissed = true;
    private ActionSheetListener mListener;
    private View mView;
    private LinearLayout mPanel;
    private ViewGroup mGroup;
     View mBg;
    private RelativeLayout mtopPanel;

    private Attributes mAttrs;
    private boolean isCancel = true;

    public void show(final FragmentManager manager, final String tag) {
        if (!mDismissed || manager.isDestroyed()) {
            return;
        }
        mDismissed = false;
        new Handler().post(new Runnable() {
            public void run() {
                FragmentTransaction ft = manager.beginTransaction();
                ft.add(ActionSheet1.this, tag);
                ft.addToBackStack(null);
                ft.commitAllowingStateLoss();
            }
        });
    }

    public void dismiss() {
        if (mDismissed) {
            return;
        }
        mDismissed = true;
        new Handler().post(new Runnable() {
            public void run() {

                if (getFragmentManager() != null) {

                    getFragmentManager().popBackStack();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(ActionSheet1.this);
                    ft.commitAllowingStateLoss();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EXTRA_DISMISSED, mDismissed);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDismissed = savedInstanceState.getBoolean(EXTRA_DISMISSED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            View focusView = getActivity().getCurrentFocus();
            if (focusView != null) {
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }

        mAttrs = readAttribute();

        mView = createView();
        mGroup = (ViewGroup) getActivity().getWindow().getDecorView();


        createItems();

        mGroup.addView(mView);


        mBg.startAnimation(createAlphaInAnimation());
        mPanel.startAnimation(createTranslationInAnimation());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        mPanel.startAnimation(createTranslationOutAnimation());
        mBg.startAnimation(createAlphaOutAnimation());
        mView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGroup.removeView(mView);
            }
        }, ALPHA_DURATION);
        if (mListener != null) {
            mListener.onDismiss(this, isCancel);
        }
        super.onDestroyView();
    }

    private Animation createTranslationInAnimation() {
        int type = TranslateAnimation.RELATIVE_TO_SELF;
        TranslateAnimation an = new TranslateAnimation(type, 0, type, 0, type,
                1, type, 0);
        an.setDuration(TRANSLATE_DURATION);
        return an;
    }

    private Animation createAlphaInAnimation() {
        AlphaAnimation an = new AlphaAnimation(0, 1);
        an.setDuration(ALPHA_DURATION);
        return an;
    }

    private Animation createTranslationOutAnimation() {
        int type = TranslateAnimation.RELATIVE_TO_SELF;
        TranslateAnimation an = new TranslateAnimation(type, 0, type, 0, type,
                0, type, 1);
        an.setDuration(TRANSLATE_DURATION);
        an.setFillAfter(true);
        return an;
    }

    private Animation createAlphaOutAnimation() {
        AlphaAnimation an = new AlphaAnimation(1, 0);
        an.setDuration(ALPHA_DURATION);
        an.setFillAfter(true);
        return an;
    }

    private View createView() {
        FrameLayout parent = new FrameLayout(getActivity());
        FrameLayout.LayoutParams param =new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        parent.setLayoutParams(param);
        param.setMargins(0,Utility.getActionBarHeight(getActivity())+60,0,0);
        mBg = new View(getActivity());
        mBg.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mBg.setBackgroundColor(Color.argb(136, 0, 0, 0));
        mBg.setId(ActionSheet1.BG_VIEW_ID);
        mBg.setOnClickListener(this);

        mPanel = new LinearLayout(getActivity());
        mtopPanel = new RelativeLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;

        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.TOP;
    //    mtopPanel.setBackgroundColor(Color.argb(136, 0, 0, 0));
       // View topChild = getLayoutInflater().inflate(R.layout.layout_pin_view, null);

      //  mtopPanel.setLayoutParams(params1);
      //  mtopPanel.addView(topChild);
        mPanel.setLayoutParams(params);
        mPanel.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(0, 0, 0, getNavBarHeight(getActivity()));
       // parent.addView(mtopPanel);
        parent.addView(mBg);
        parent.addView(mPanel);
        parent.bringToFront();
        return parent;
    }

    public int getNavBarHeight(Context context) {
        int navigationBarHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Resources rs = context.getResources();
            int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
            if (id > 0 && checkDeviceHasNavigationBar(context)) {
                navigationBarHeight = rs.getDimensionPixelSize(id);
            }
        }
        return navigationBarHeight;
    }

    private boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasNavigationBar;

    }

    private void createItems() {
        String[] titles = getOtherButtonTitles();
        if (titles != null) {
            for (int i = 0; i < titles.length; i++) {
                Button bt = new Button(getActivity());
                bt.setId(CANCEL_BUTTON_ID + i + 1);
                bt.setAllCaps(false);
                bt.setOnClickListener(this);
                bt.setBackgroundDrawable(getOtherButtonBg(titles, i));
                bt.setText(titles[i]);

                if(titles[i].toLowerCase().contains("DELETE".toLowerCase())){
                    bt.setTextColor(Color.RED);
                }
                else {
                    bt.setTextColor(mAttrs.otherButtonTextColor);
                }
                bt.setTextSize(TypedValue.COMPLEX_UNIT_PX, mAttrs.actionSheetTextSize);
                if (i > 0) {
                    LinearLayout.LayoutParams params = createButtonLayoutParams();
                    params.topMargin = mAttrs.otherButtonSpacing;
                    mPanel.addView(bt, params);
                } else {
                    mPanel.addView(bt);
                }
            }
        }
        Button bt = new Button(getActivity());
        bt.getPaint().setFakeBoldText(true);
        bt.setTextSize(TypedValue.COMPLEX_UNIT_PX, mAttrs.actionSheetTextSize);
        bt.setId(ActionSheet1.CANCEL_BUTTON_ID);
        bt.setBackgroundDrawable(mAttrs.cancelButtonBackground);
        bt.setText(getCancelButtonTitle());
        bt.setAllCaps(false);
        bt.setTextColor(mAttrs.cancelButtonTextColor);
        bt.setOnClickListener(this);
        LinearLayout.LayoutParams params = createButtonLayoutParams();
        params.topMargin = mAttrs.cancelButtonMarginTop;
        mPanel.addView(bt, params);
      //  mtopPanel.setBackgroundDrawable(mAttrs.background);
        mPanel.setBackgroundDrawable(mAttrs.background);
        mPanel.setPadding(mAttrs.padding, mAttrs.padding, mAttrs.padding,
                mAttrs.padding);
    }

    public LinearLayout.LayoutParams createButtonLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        return params;
    }

    private Drawable getOtherButtonBg(String[] titles, int i) {
        if (titles.length == 1) {
            return mAttrs.otherButtonSingleBackground;
        }
        if (titles.length == 2) {
            switch (i) {
                case 0:
                    return mAttrs.otherButtonTopBackground;
                case 1:
                    return mAttrs.otherButtonBottomBackground;
            }
        }
        if (titles.length > 2) {
            if (i == 0) {
                return mAttrs.otherButtonTopBackground;
            }
            if (i == (titles.length - 1)) {
                return mAttrs.otherButtonBottomBackground;
            }
            return mAttrs.getOtherButtonMiddleBackground();
        }
        return null;
    }

    private Attributes readAttribute() {
        Attributes attrs = new Attributes(getActivity());
      /*  TypedArray a = getActivity().getTheme().obtainStyledAttributes(null,
                R.styleable.ActionSheet, R.attr.actionSheetStyle, 0);
        Drawable background = a
                .getDrawable(R.styleable.ActionSheet_actionSheetBackground);
        if (background != null) {
            attrs.background = background;
        }
        Drawable cancelButtonBackground = a
                .getDrawable(R.styleable.ActionSheet_cancelButtonBackground);
        if (cancelButtonBackground != null) {
            attrs.cancelButtonBackground = cancelButtonBackground;
        }
        Drawable otherButtonTopBackground = a
                .getDrawable(R.styleable.ActionSheet_otherButtonTopBackground);
        if (otherButtonTopBackground != null) {
            attrs.otherButtonTopBackground = otherButtonTopBackground;
        }
        Drawable otherButtonMiddleBackground = a
                .getDrawable(R.styleable.ActionSheet_otherButtonMiddleBackground);
        if (otherButtonMiddleBackground != null) {
            attrs.otherButtonMiddleBackground = otherButtonMiddleBackground;
        }
        Drawable otherButtonBottomBackground = a
                .getDrawable(R.styleable.ActionSheet_otherButtonBottomBackground);
        if (otherButtonBottomBackground != null) {
            attrs.otherButtonBottomBackground = otherButtonBottomBackground;
        }
        Drawable otherButtonSingleBackground = a
                .getDrawable(R.styleable.ActionSheet_otherButtonSingleBackground);
        if (otherButtonSingleBackground != null) {
            attrs.otherButtonSingleBackground = otherButtonSingleBackground;
        }
        attrs.cancelButtonTextColor = a.getColor(
                R.styleable.ActionSheet_cancelButtonTextColor,
                attrs.cancelButtonTextColor);
        attrs.otherButtonTextColor = a.getColor(
                R.styleable.ActionSheet_otherButtonTextColor,
                attrs.otherButtonTextColor);
        attrs.padding = (int) a.getDimension(
                R.styleable.ActionSheet_actionSheetPadding, attrs.padding);
        attrs.otherButtonSpacing = (int) a.getDimension(
                R.styleable.ActionSheet_otherButtonSpacing,
                attrs.otherButtonSpacing);
        attrs.cancelButtonMarginTop = (int) a.getDimension(
                R.styleable.ActionSheet_cancelButtonMarginTop,
                attrs.cancelButtonMarginTop);
        attrs.actionSheetTextSize = a.getDimensionPixelSize(R.styleable.ActionSheet_actionSheetTextSize, (int) attrs.actionSheetTextSize);

        a.recycle();*/
        return attrs;
    }

    private String getCancelButtonTitle() {
        return getArguments().getString(ARG_CANCEL_BUTTON_TITLE);
    }

    private String[] getOtherButtonTitles() {
        return getArguments().getStringArray(ARG_OTHER_BUTTON_TITLES);
    }

    private boolean getCancelableOnTouchOutside() {
        return getArguments().getBoolean(ARG_CANCELABLE_ONTOUCHOUTSIDE);
    }

    public void setActionSheetListener(ActionSheetListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ActionSheet1.BG_VIEW_ID && !getCancelableOnTouchOutside()) {
            return;
        }
        dismiss();
        if (v.getId() != ActionSheet1.CANCEL_BUTTON_ID && v.getId() != ActionSheet1.BG_VIEW_ID) {
            if (mListener != null) {
                mListener.onOtherButtonClick(this, v.getId() - CANCEL_BUTTON_ID
                        - 1);
            }
            isCancel = false;
        }
    }

    public static Builder createBuilder(Context context,
                                        FragmentManager fragmentManager) {
        return new Builder(context, fragmentManager);
    }



    private static class Attributes {
        private final Context mContext;

        public Attributes(Context context) {
            mContext = context;
            this.background = new ColorDrawable(Color.RED);
            this.cancelButtonBackground = new ColorDrawable(Color.BLACK);
            ColorDrawable gray = new ColorDrawable(Color.GRAY);
            this.otherButtonTopBackground = gray;
            this.otherButtonMiddleBackground = gray;
            this.otherButtonBottomBackground = gray;
            this.otherButtonSingleBackground = gray;
            this.cancelButtonTextColor = Color.WHITE;
            this.otherButtonTextColor = Color.BLACK;
            this.padding = dp2px(20);
            this.otherButtonSpacing = dp2px(2);
            this.cancelButtonMarginTop = dp2px(10);
            this.actionSheetTextSize = dp2px(16);
        }

        private int dp2px(int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    dp, mContext.getResources().getDisplayMetrics());
        }

        public Drawable getOtherButtonMiddleBackground() {
//            if (otherButtonMiddleBackground instanceof StateListDrawable) {
//                TypedArray a = mContext.getTheme().obtainStyledAttributes(null,
//                        R.styleable.ActionSheet, R.attr.actionSheetStyle, 0);
//                otherButtonMiddleBackground = a
//                        .getDrawable(R.styleable.ActionSheet_otherButtonMiddleBackground);
//                a.recycle();
//            }
            return otherButtonMiddleBackground;
        }

        Drawable background;
        Drawable cancelButtonBackground;
        Drawable otherButtonTopBackground;
        Drawable otherButtonMiddleBackground;
        Drawable otherButtonBottomBackground;
        Drawable otherButtonSingleBackground;
        int cancelButtonTextColor;
        int otherButtonTextColor;
        int padding;
        int otherButtonSpacing;
        int cancelButtonMarginTop;
        float actionSheetTextSize;
    }

    public static class Builder {

        private final Context mContext;
        private final FragmentManager mFragmentManager;
        private String mCancelButtonTitle;
        private String[] mOtherButtonTitles;
        private String mTag = "actionSheet";
        private boolean mCancelableOnTouchOutside;
        private ActionSheetListener mListener;
        ActionSheet1 actionSheet;
        public Builder(Context context, FragmentManager fragmentManager) {
            mContext = context;
            mFragmentManager = fragmentManager;
        }

        public Builder setCancelButtonTitle(String title) {
            mCancelButtonTitle = title;
            return this;
        }

        public Builder setCancelButtonTitle(int strId) {
            return setCancelButtonTitle(mContext.getString(strId));
        }

        public Builder setOtherButtonTitles(String... titles) {
            mOtherButtonTitles = titles;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder setListener(ActionSheetListener listener) {
            this.mListener = listener;
            return this;
        }

        public Builder setCancelableOnTouchOutside(boolean cancelable) {
            mCancelableOnTouchOutside = cancelable;
            return this;
        }

        public Bundle prepareArguments() {
            Bundle bundle = new Bundle();
            bundle.putString(ARG_CANCEL_BUTTON_TITLE, mCancelButtonTitle);
            bundle.putStringArray(ARG_OTHER_BUTTON_TITLES, mOtherButtonTitles);
            bundle.putBoolean(ARG_CANCELABLE_ONTOUCHOUTSIDE,
                    mCancelableOnTouchOutside);
            return bundle;
        }

        public ActionSheet1 show() {
            actionSheet = (ActionSheet1) Fragment.instantiate(
                    mContext, ActionSheet1.class.getName(), prepareArguments());
            actionSheet.setActionSheetListener(mListener);
            actionSheet.show(mFragmentManager, mTag);
            return actionSheet;
        }

        public void hide() {
           /* ActionSheet1 actionSheet = (ActionSheet1) Fragment.instantiate(
                    mContext, ActionSheet1.class.getName(), prepareArguments());
            actionSheet.setActionSheetListener(mListener);
            actionSheet.(mFragmentManager, mTag);
            return actionSheet;*/

            actionSheet.dismiss();

        }

    }

    public interface ActionSheetListener {

        void onDismiss(ActionSheet1 actionSheet, boolean isCancel);

        void onOtherButtonClick(ActionSheet1 actionSheet, int index);
    }

}
