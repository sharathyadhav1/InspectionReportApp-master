package com.dolabs.emircom.model;

import com.dolabs.emircom.localdatabase.AppDatabase;
import com.dolabs.emircom.localdatabase.BaseTaskRDBModel;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import java.io.Serializable;

@Table(database = AppDatabase.class, name = UserModel.TABLENAME, updateConflict = ConflictAction.REPLACE)
public class UserModel extends BaseTaskRDBModel implements Serializable,Cloneable {

    public static final String TABLENAME = "userModel";

    @PrimaryKey
    public String id;

    @Column
    public String name;

    @Column
    public String username;

    @Column
    public String email;

    @Column
    public String phone_number;

    @Column
    public String request_date;

    @Column
    public String token;

    @Column
    public String role;

    public int success;

    public UserModel message;

    public int verified;

    public int office_relation_id;


    public UserModel() {

    }
}