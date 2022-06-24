package com.dolabs.emircom.connector;


import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Jose on 3/16/16.
 */
public class ConnectorTransfer {

    private boolean disableDebugging;

    private ContentType contentType = ContentType.APPLICATION_JSON;

    private HTTPActionType actionType;
    private boolean asyncEnabled;

    private String requestUrl;
    private LinkedHashMap<String, String> requestHeaders;
    private LinkedHashMap<String, String> pathParams;
    private LinkedHashMap<String, String> queryParams;
    private LinkedHashMap<String, Object> requestBodyMap;
    private LinkedHashMap<String, String> requestFileBodyMap;

    private String customAuthorization;
    private String customRequestString;

    private String functionName;
    private String simulatedResponseFile;
    private boolean simulateResponseError;

    private String responseCode;
    private List<FilePart> fileParts;

    private boolean returnRawResponse = false;
    private Class responseModelClass;

    public boolean isDisableDebugging() {
        return disableDebugging;
    }

    public void setDisableDebugging(boolean disableDebugging) {
        this.disableDebugging = disableDebugging;
    }

    public HTTPActionType getActionType() {
        return actionType;
    }

    public void setActionType(HTTPActionType actionType) {
        this.actionType = actionType;
    }

    public String getSimulatedResponseFile() {
        return simulatedResponseFile;
    }

    public void setSimulatedResponseFile(String simulatedResponseFile) {
        this.simulatedResponseFile = simulatedResponseFile;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public LinkedHashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(LinkedHashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public LinkedHashMap<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(LinkedHashMap<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public LinkedHashMap<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(LinkedHashMap<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public LinkedHashMap<String, Object> getRequestBodyMap() {
        return requestBodyMap;
    }

    public void setRequestBodyMap(LinkedHashMap<String, Object> requestBodyMap) {
        this.requestBodyMap = requestBodyMap;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public boolean isSimulateResponseError() {
        return simulateResponseError;
    }

    public void setSimulateResponseError(boolean simulateResponseError) {
        this.simulateResponseError = simulateResponseError;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public LinkedHashMap<String, String> getRequestFileBodyMap() {
        return requestFileBodyMap;
    }

    public void setRequestFileBodyMap(LinkedHashMap<String, String> requestFileBodyMap) {
        this.requestFileBodyMap = requestFileBodyMap;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setCustomAuthorization(String customAuthorization) {
        this.customAuthorization = customAuthorization;
    }
    public String getCustomAuthorization() {
        return  customAuthorization ;
    }

    public String getCustomRequestString() {
        return customRequestString;
    }

    public void setCustomRequestString(String customRequestString) {
        this.customRequestString = customRequestString;
    }

    public boolean isReturnRawResponse() {
        return returnRawResponse;
    }

    public void setReturnRawResponse(boolean returnRawResponse) {
        this.returnRawResponse = returnRawResponse;
    }

    public Class getResponseModelClass() {
        return responseModelClass;
    }

    public void setResponseModelClass(Class responseModelClass) {
        this.responseModelClass = responseModelClass;
    }

    public List<FilePart> getFileParts() {
        return fileParts;
    }

    public void setFileParts(List<FilePart> fileParts) {
        this.fileParts = fileParts;
    }

    public static class FilePart {

        public String fileKey;
        public String fileUri;

        public FilePart(String fileKey, String fileUri) {

            this.fileKey = fileKey;
            this.fileUri = fileUri;
        }
    }
}
