package com.dolabs.emircom.model;

import java.io.Serializable;

public class MediaModel implements Serializable{



    public String id;

    public String url;

    public String user_id;

    public String title;

    public String description;

    public String price;

    public int is_active;

    public String uploaded_time;
    public int draawable;
    public int category_id;



    public MediaModel(String productid,String productName,int drawable) {
        this.id =productid;
        this.title =productName;
        this.draawable =drawable;

    }
}
