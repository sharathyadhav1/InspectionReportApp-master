package com.dolabs.emircom.connector;

/**
 * Created by Jose on 3/16/16.
 */
public interface ServiceResponse {

    void onSuccess(int responseCode, Object response, boolean isTokenRefresh);

    void onProgressUpdate(Long... progress);

    void onError(int responseCode, String errorCode, String errorMessage);
}