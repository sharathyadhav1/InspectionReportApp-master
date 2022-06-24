package com.dolabs.emircom.localdatabase;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {
    public static final String NAME = "emricom";

    public static final int VERSION = 1;


}