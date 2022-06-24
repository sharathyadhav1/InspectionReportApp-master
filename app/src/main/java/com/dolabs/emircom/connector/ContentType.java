package com.dolabs.emircom.connector;

public enum ContentType {

    APPLICATION_JSON(1),
    APPLICATION_URL_ENCODED(2),
    MULTIPART_FORM_DATA(3);

    private final int value;

    private ContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String getHeaderKey() {
        return "Content-Type";
    }

    public String headerName() {

        String displayName = "application/json";

        switch (value) {

            case 1:
                displayName = "application/json";
                break;

            case 2:
                displayName = "application/x-www-form-urlencoded";
                break;

            case 3:
                displayName = "multipart/form-data";
                break;
        }

        return displayName;
    }
}