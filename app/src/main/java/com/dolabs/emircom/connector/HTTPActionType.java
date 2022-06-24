package com.dolabs.emircom.connector;

/**
 * Created by Jose on 3/16/16.
 */
public enum HTTPActionType {

    GET(1),
    POST(2),
    PUT(3),
    DELETE(4);

    private final int value;

    private HTTPActionType (int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
