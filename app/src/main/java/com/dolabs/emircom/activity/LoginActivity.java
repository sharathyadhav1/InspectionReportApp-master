package com.dolabs.emircom.activity;


import static com.dolabs.emircom.utils.Constants.BASE_URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;


import com.dolabs.emircom.R;
import com.dolabs.emircom.connector.ConnectorTransfer;
import com.dolabs.emircom.connector.ContentType;
import com.dolabs.emircom.connector.HTTPActionType;
import com.dolabs.emircom.connector.HTTPConnector;
import com.dolabs.emircom.connector.ServiceResponse;
import com.dolabs.emircom.databinding.ActivityLoginBinding;
import com.dolabs.emircom.model.UserModel;
import com.dolabs.emircom.utils.Commons;
import com.dolabs.emircom.utils.Constants;
import com.dolabs.emircom.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LoginActivity extends AppActivity {

    private final String TAG = this.getClass().getSimpleName();


    ActivityLoginBinding binding;
    boolean is_from_home_screen=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        is_from_home_screen = getIntent().getBooleanExtra("is_from_home_screen",false);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);


        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!binding.etUserName.getText().toString().isEmpty() && !binding.etPassword.getText().toString().isEmpty())
                {

                    processLogin();
                }
                else
                {
                   // Utility.showSnackBar(mActivity, mActivity.rootView, getString(R.string.plaes_enter_username_password), -1, R.color.red, false,null);
                    Intent intent = new Intent(mActivity, InspectorEntryActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            }
        });
        binding.tvGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel userModel = new UserModel();
                userModel.username="guest";
                userModel.role="2";
                userModel.save();
                showNextScreen();

            }
        });

    }

    private void processLogin() {

        try {



            showProgress(true);
            ConnectorTransfer connectorTransfer = new ConnectorTransfer();
            connectorTransfer.setActionType(HTTPActionType.POST);
            connectorTransfer.setContentType(ContentType.APPLICATION_URL_ENCODED);
            connectorTransfer.setRequestUrl(BASE_URL);

            LinkedHashMap<String, Object> reqMap = new LinkedHashMap<>();
            reqMap.put("action", "getLogin");
            reqMap.put("username", binding.etUserName.getText().toString());
            reqMap.put("password", binding.etPassword.getText().toString());

            connectorTransfer.setRequestBodyMap(reqMap);

            HTTPConnector httpConnector = new HTTPConnector(mContext, connectorTransfer, new ServiceResponse() {

                @Override
                public void onSuccess(int responseCode, Object response, boolean istokenrefresh) {

                    try {

                        showProgress(false);

                        if(responseCode == 200)
                        {
                            JSONObject json =  new JSONObject(response.toString());
                            boolean status = json.getBoolean("status");


                            if(status)
                            {

                                String data =  json.getString("data");

                               /* SQLite.delete()
                                        .from(CategoryModel.class)
                                        .query();*/

                                UserModel userModel = new Gson().fromJson(data, new TypeToken<UserModel>() {}.getType());
                                if(userModel!=null ) {
                                    userModel.save();
                                    showNextScreen();

                                }
                                else
                                {
                                    Commons.showSnackbar(rootView,getString(R.string.invalid_credentials_please_try_again), Color.parseColor(Constants.COLOR_ERROR), R.drawable.ic_error);

                                }

                            }
                            else
                            {
                                Commons.showSnackbar(rootView,getString(R.string.invalid_credentials_please_try_again), Color.parseColor(Constants.COLOR_ERROR), R.drawable.ic_error);

                            }



                        }
                        else
                        {
                            Commons.showSnackbar(rootView,getResources().getString(R.string.error_while_processing), Color.parseColor(Constants.COLOR_ERROR), R.drawable.ic_error);
                        }


                    }
                    catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onProgressUpdate(Long... progress) {

                }

                @Override
                public void onError(int responseCode, String errorCode, String errorMessage) {


                    Commons.showSnackbar(rootView,getResources().getString(R.string.error_while_processing), Color.parseColor(Constants.COLOR_ERROR), R.drawable.ic_error);

                    showProgress(false);
                }
            });

            httpConnector.execute();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void showNextScreen() {
        if(is_from_home_screen)
        {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            onBackPressed();
        }
        else
        {
             /*Intent intent = new Intent(mActivity, HomeCategryScreenActivity.class);
            startActivity(intent);
            finishAffinity();*/
        }
    }


}