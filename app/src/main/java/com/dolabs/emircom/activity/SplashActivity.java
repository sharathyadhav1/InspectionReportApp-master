package com.dolabs.emircom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dolabs.emircom.R;
import com.dolabs.emircom.model.UserModel;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends AppActivity {

    private final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                openNextScreen();
            }
        }, 10);
    }

    private void openNextScreen() {



        UserModel userModel =  SQLite.select()
                .from(UserModel.class)
                .querySingle();
        if(userModel!=null )
        {
           /* Intent intent = new Intent(this, HomeCategryScreenActivity.class);
            startActivity(intent);
            finishAffinity();*/
        }
        else
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        }




    }



}