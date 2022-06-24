package com.dolabs.emircom.model;

import java.io.Serializable;

public class NotificationModel  implements Serializable{



    public String notification_id;

    public String task_id;

    public String notification_type;

    public String user_id;

    public String assignee_id;

    public String team_id;

    public String description;

    public String notification_date;

    public String task_name;

    public String office_name;


    public NotificationModel() {

    }
}
